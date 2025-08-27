package kavlo.sft.mobile.service

import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.*
import java.util.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import com.google.gson.Gson
import kavlo.sft.mobile.data.SensorData
import kavlo.sft.mobile.data.ConnectionStatus

class ESP32BluetoothService(private val context: Context) {
    companion object {
        private const val TAG = "ESP32Service"
        // UUID untuk SPP (Serial Port Profile) - standard untuk ESP32 Classic Bluetooth
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val RECONNECT_DELAY = 3000L
        private const val MAX_RECONNECT_ATTEMPTS = 3
        private const val READ_TIMEOUT = 1000
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    private val gson = Gson()
    private var reconnectAttempts = 0
    private var isReconnecting = false
    private var readingJob: Job? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // State flows
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            _errorMessage.value = "Bluetooth not supported on this device"
            Log.e(TAG, "Bluetooth not supported")
        }
    }

    fun connectToDevice(deviceAddress: String): Boolean {
        if (bluetoothAdapter == null) {
            _errorMessage.value = "Bluetooth adapter not available"
            Log.e(TAG, "Bluetooth adapter is null")
            return false
        }

        if (!bluetoothAdapter!!.isEnabled) {
            _errorMessage.value = "Bluetooth is not enabled"
            Log.e(TAG, "Bluetooth is not enabled")
            return false
        }

        // Validate MAC address format
        if (!BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
            _errorMessage.value = "Invalid MAC address format: $deviceAddress"
            Log.e(TAG, "Invalid MAC address: $deviceAddress")
            return false
        }

        // Disconnect existing connection
        disconnect()

        serviceScope.launch {
            try {
                _errorMessage.value = null
                _connectionStatus.value = false
                reconnectAttempts = 0
                isReconnecting = false

                Log.d(TAG, "Attempting to connect to $deviceAddress")

                val device = bluetoothAdapter!!.getRemoteDevice(deviceAddress)
                Log.d(TAG, "Device found: ${device.name ?: "Unknown"}")

                // Cancel discovery to improve connection speed
                if (bluetoothAdapter!!.isDiscovering) {
                    bluetoothAdapter!!.cancelDiscovery()
                }

                // Create socket using SPP UUID
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)

                Log.d(TAG, "Connecting to socket...")
                bluetoothSocket?.connect()

                if (bluetoothSocket?.isConnected == true) {
                    inputStream = bluetoothSocket?.inputStream
                    outputStream = bluetoothSocket?.outputStream
                    isConnected = true
                    _connectionStatus.value = true

                    Log.d(TAG, "Successfully connected to ESP32")

                    // Start reading data
                    startDataReading()

                } else {
                    throw IOException("Socket connection failed")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Connection failed: ${e.message}", e)
                _errorMessage.value = "Connection failed: ${e.message}"
                _connectionStatus.value = false

                // Try fallback connection method
                if (reconnectAttempts == 0) {
                    tryFallbackConnection(deviceAddress)
                }
            }
        }

        return true
    }

    private suspend fun tryFallbackConnection(deviceAddress: String) {
        try {
            Log.d(TAG, "Trying fallback connection method...")

            val device = bluetoothAdapter!!.getRemoteDevice(deviceAddress)

            // Use reflection to try createInsecureRfcommSocket
            val method = device.javaClass.getMethod("createInsecureRfcommSocketToServiceRecord", UUID::class.java)
            bluetoothSocket = method.invoke(device, SPP_UUID) as BluetoothSocket

            bluetoothSocket?.connect()

            if (bluetoothSocket?.isConnected == true) {
                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream
                isConnected = true
                _connectionStatus.value = true

                Log.d(TAG, "Fallback connection successful")
                startDataReading()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Fallback connection also failed: ${e.message}", e)
            _errorMessage.value = "All connection methods failed: ${e.message}"
        }
    }

    private fun startDataReading() {
        readingJob?.cancel()
        readingJob = serviceScope.launch {
            val buffer = ByteArray(1024)
            var dataBuffer = ""

            try {
                while (isConnected && bluetoothSocket?.isConnected == true) {
                    try {
                        val bytesRead = inputStream?.read(buffer) ?: 0
                        if (bytesRead > 0) {
                            val receivedData = String(buffer, 0, bytesRead)
                            dataBuffer += receivedData

                            // Process complete JSON messages
                            while (dataBuffer.contains('\n')) {
                                val endIndex = dataBuffer.indexOf('\n')
                                val jsonMessage = dataBuffer.substring(0, endIndex).trim()
                                dataBuffer = dataBuffer.substring(endIndex + 1)

                                if (jsonMessage.isNotEmpty()) {
                                    parseAndUpdateSensorData(jsonMessage)
                                }
                            }
                        }
                    } catch (e: IOException) {
                        Log.w(TAG, "Read interrupted: ${e.message}")
                        if (isConnected) {
                            scheduleReconnect()
                        }
                        break
                    }

                    delay(100) // Small delay to prevent busy waiting
                }
            } catch (e: Exception) {
                Log.e(TAG, "Data reading error: ${e.message}", e)
                if (isConnected) {
                    scheduleReconnect()
                }
            }
        }
    }

    private fun parseAndUpdateSensorData(jsonMessage: String) {
        try {
            Log.d(TAG, "Received JSON: $jsonMessage")

            val sensorData = gson.fromJson(jsonMessage, SensorData::class.java)
            _sensorData.value = sensorData.copy(timestamp = System.currentTimeMillis())

            Log.d(TAG, "Sensor data updated: HR=${sensorData.heartRate}, SpO2=${sensorData.oxygenLevel}, Temp=${sensorData.temperature}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON: $jsonMessage", e)

            // Try to parse as simple format: "HR:72,SpO2:98,TEMP:36.5,STRESS:15,BAT:85"
            try {
                parseSimpleFormat(jsonMessage)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to parse simple format too", e2)
                _errorMessage.value = "Invalid data format: $jsonMessage"
            }
        }
    }

    private fun parseSimpleFormat(data: String) {
        val parts = data.split(",")
        var heartRate = 0
        var oxygenLevel = 0
        var temperature = 0f
        var stressLevel = 0
        var batteryLevel = 0

        for (part in parts) {
            val keyValue = part.split(":")
            if (keyValue.size == 2) {
                val key = keyValue[0].trim()
                val value = keyValue[1].trim()

                when (key.uppercase()) {
                    "HR", "HEARTRATE" -> heartRate = value.toIntOrNull() ?: 0
                    "SPO2", "OXYGEN" -> oxygenLevel = value.toIntOrNull() ?: 0
                    "TEMP", "TEMPERATURE" -> temperature = value.toFloatOrNull() ?: 0f
                    "STRESS" -> stressLevel = value.toIntOrNull() ?: 0
                    "BAT", "BATTERY" -> batteryLevel = value.toIntOrNull() ?: 0
                }
            }
        }

        val sensorData = SensorData(
            heartRate = heartRate,
            oxygenLevel = oxygenLevel,
            temperature = temperature,
            stressLevel = stressLevel,
            batteryLevel = batteryLevel,
            timestamp = System.currentTimeMillis()
        )

        _sensorData.value = sensorData
        Log.d(TAG, "Parsed simple format: $sensorData")
    }

    private fun scheduleReconnect() {
        if (isReconnecting || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            return
        }

        isReconnecting = true
        reconnectAttempts++

        Log.d(TAG, "Scheduling reconnect attempt $reconnectAttempts")

        serviceScope.launch {
            delay(RECONNECT_DELAY)

            if (reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
                Log.d(TAG, "Attempting reconnect...")
                // Get the last connected device address - you might want to store this
                // For now, we'll just set the error message
                _errorMessage.value = "Connection lost, attempting to reconnect..."
            } else {
                isReconnecting = false
                _errorMessage.value = "Failed to reconnect after $MAX_RECONNECT_ATTEMPTS attempts"
                Log.e(TAG, "Max reconnect attempts reached")
            }
        }
    }

    fun sendCommand(command: String): Boolean {
        return try {
            if (isConnected && outputStream != null) {
                outputStream?.write("$command\n".toByteArray())
                outputStream?.flush()
                Log.d(TAG, "Sent command: $command")
                true
            } else {
                Log.w(TAG, "Cannot send command - not connected")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send command: ${e.message}", e)
            false
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting from ESP32")

        isConnected = false
        isReconnecting = false
        reconnectAttempts = MAX_RECONNECT_ATTEMPTS

        readingJob?.cancel()

        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing connections: ${e.message}")
        }

        inputStream = null
        outputStream = null
        bluetoothSocket = null

        _connectionStatus.value = false
        _errorMessage.value = null

        Log.d(TAG, "Disconnected successfully")
    }

    fun isDeviceConnected(): Boolean {
        return isConnected && bluetoothSocket?.isConnected == true
    }

    fun getConnectedDeviceName(): String? {
        return bluetoothSocket?.remoteDevice?.name
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up ESP32BluetoothService")
        disconnect()
        serviceScope.cancel()
    }

    // Function to scan for ESP32 devices
    fun scanForDevices(callback: (List<BluetoothDevice>) -> Unit) {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            callback(emptyList())
            return
        }

        val pairedDevices = bluetoothAdapter!!.bondedDevices
        val esp32Devices = pairedDevices.filter { device ->
            device.name?.contains("ESP32", ignoreCase = true) == true ||
                    device.name?.contains("KAVLO", ignoreCase = true) == true ||
                    device.name?.contains("SmartHeadband", ignoreCase = true) == true
        }

        Log.d(TAG, "Found ${esp32Devices.size} potential ESP32 devices")
        callback(esp32Devices)
    }
}