package kavlo.sft.mobile.data

import com.google.gson.annotations.SerializedName

data class SensorData(
    @SerializedName("heartRate") val heartRate: Int = 0,
    @SerializedName("oxygenLevel") val oxygenLevel: Int = 0,
    @SerializedName("temperature") val temperature: Float = 0f,
    @SerializedName("stressLevel") val stressLevel: Int = 0,
    @SerializedName("batteryLevel") val batteryLevel: Int = 0,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

data class ConnectionStatus(
    val isConnected: Boolean = false,
    val deviceName: String = "",
    val signalStrength: Int = 0,
    val lastUpdate: Long = 0L
)

data class DeviceInfo(
    val name: String = "KAVLO_SmartHeadband",
    val macAddress: String = "",
    val firmwareVersion: String = "1.0.0",
    val batteryLevel: Int = 0
)