package kavlo.sft.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kavlo.sft.mobile.data.SensorData
import kavlo.sft.mobile.data.SensorDataRealmRepository
import kavlo.sft.mobile.data.ConnectionStatus

class SensorDataViewModel(
    private val repository: SensorDataRealmRepository
) : ViewModel() {
    
    // State for all sensor data
    private val _sensorDataList = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorDataList: StateFlow<List<SensorData>> = _sensorDataList.asStateFlow()
    
    // State for latest sensor reading
    private val _latestSensorData = MutableStateFlow<SensorData?>(null)
    val latestSensorData: StateFlow<SensorData?> = _latestSensorData.asStateFlow()
    
    // State for connection status
    private val _connectionStatus = MutableStateFlow<ConnectionStatus?>(null)
    val connectionStatus: StateFlow<ConnectionStatus?> = _connectionStatus.asStateFlow()
    
    // State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Start observing data changes
        observeAllSensorData()
        observeConnectionStatus()
        loadLatestData()
    }
    
    private fun observeAllSensorData() {
        viewModelScope.launch {
            repository.getAllSensorDataFlow().collect { dataList ->
                _sensorDataList.value = dataList
                if (dataList.isNotEmpty()) {
                    _latestSensorData.value = dataList.first() // Most recent
                }
            }
        }
    }
    
    private fun observeConnectionStatus() {
        viewModelScope.launch {
            repository.getConnectionStatusFlow().collect { status ->
                _connectionStatus.value = status
            }
        }
    }
    
    private fun loadLatestData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val latest = repository.getLatestSensorData()
                latest?.let { _latestSensorData.value = it }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Add new sensor data (from Bluetooth/device)
    fun addSensorData(
        heartRate: Int,
        oxygenLevel: Int,
        temperature: Float,
        stressLevel: Int,
        batteryLevel: Int
    ) {
        viewModelScope.launch {
            repository.insertSensorData(
                heartRate = heartRate,
                oxygenLevel = oxygenLevel,
                temperature = temperature,
                stressLevel = stressLevel,
                batteryLevel = batteryLevel
            )
        }
    }
    
    // Get recent data (last N hours)
    fun loadRecentData(hours: Int) {
        viewModelScope.launch {
            repository.getSensorDataFromLastHours(hours).collect { dataList ->
                _sensorDataList.value = dataList
            }
        }
    }
    
    // Get filtered data
    fun loadFilteredData(
        minHeartRate: Int? = null,
        minOxygenLevel: Int? = null,
        maxStressLevel: Int? = null
    ) {
        viewModelScope.launch {
            repository.getFilteredSensorData(
                minHeartRate = minHeartRate,
                minOxygenLevel = minOxygenLevel,
                maxStressLevel = maxStressLevel
            ).collect { dataList ->
                _sensorDataList.value = dataList
            }
        }
    }
    
    // Update device connection
    fun updateConnectionStatus(
        isConnected: Boolean,
        deviceName: String,
        signalStrength: Int
    ) {
        viewModelScope.launch {
            repository.updateConnectionStatus(isConnected, deviceName, signalStrength)
        }
    }
    
    // Clean old data
    fun cleanOldData(daysToKeep: Int = 30) {
        viewModelScope.launch {
            repository.cleanOldData(daysToKeep)
        }
    }
    
    // Get health insights
    fun getHealthInsights(): HealthInsights? {
        val currentData = _sensorDataList.value
        if (currentData.isEmpty()) return null
        
        val recent = currentData.take(10) // Last 10 readings
        return HealthInsights(
            avgHeartRate = recent.map { it.heartRate }.average().toInt(),
            avgOxygenLevel = recent.map { it.oxygenLevel }.average().toInt(),
            avgTemperature = recent.map { it.temperature }.average().toFloat(),
            avgStressLevel = recent.map { it.stressLevel }.average().toInt(),
            totalReadings = currentData.size,
            lastUpdate = recent.firstOrNull()?.timestamp ?: 0L
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}

// Data class for health insights
data class HealthInsights(
    val avgHeartRate: Int,
    val avgOxygenLevel: Int,
    val avgTemperature: Float,
    val avgStressLevel: Int,
    val totalReadings: Int,
    val lastUpdate: Long
)

// ViewModel Factory
class SensorDataViewModelFactory(
    private val repository: SensorDataRealmRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorDataViewModel::class.java)) {
            return SensorDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}