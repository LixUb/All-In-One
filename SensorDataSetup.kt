// build.gradle (Module: app)
/*
dependencies {
    implementation 'io.realm.kotlin:library-base:1.11.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
}
*/

package kavlo.sft.mobile.data

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

// Realm Object (replaces your data class)
class SensorDataRealm : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var heartRate: Int = 0
    var oxygenLevel: Int = 0
    var temperature: Float = 0f
    var stressLevel: Int = 0
    var batteryLevel: Int = 0
    var timestamp: Long = System.currentTimeMillis()
}

class ConnectionStatusRealm : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var isConnected: Boolean = false
    var deviceName: String = ""
    var signalStrength: Int = 0
    var lastUpdate: Long = 0L
}

class DeviceInfoRealm : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = "KAVLO_SmartHeadband"
    var macAddress: String = ""
    var firmwareVersion: String = "1.0.0"
    var batteryLevel: Int = 0
}

// Repository class for Realm operations
class SensorDataRealmRepository {
    
    private val config = RealmConfiguration.Builder(
        schema = setOf(SensorDataRealm::class, ConnectionStatusRealm::class, DeviceInfoRealm::class)
    ).build()
    
    private val realm = Realm.open(config)
    
    // Insert new sensor data
    suspend fun insertSensorData(
        heartRate: Int,
        oxygenLevel: Int,
        temperature: Float,
        stressLevel: Int,
        batteryLevel: Int
    ) {
        realm.write {
            val sensorData = SensorDataRealm().apply {
                this.heartRate = heartRate
                this.oxygenLevel = oxygenLevel
                this.temperature = temperature
                this.stressLevel = stressLevel
                this.batteryLevel = batteryLevel
                this.timestamp = System.currentTimeMillis()
            }
            copyToRealm(sensorData)
        }
    }
    
    // Get all sensor data as Flow (reactive)
    fun getAllSensorDataFlow(): Flow<List<SensorData>> {
        return realm.query<SensorDataRealm>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .asFlow()
            .map { results ->
                results.list.map { realmObj ->
                    SensorData(
                        heartRate = realmObj.heartRate,
                        oxygenLevel = realmObj.oxygenLevel,
                        temperature = realmObj.temperature,
                        stressLevel = realmObj.stressLevel,
                        batteryLevel = realmObj.batteryLevel,
                        timestamp = realmObj.timestamp
                    )
                }
            }
    }
    
    // Get latest sensor data
    suspend fun getLatestSensorData(): SensorData? {
        val latest = realm.query<SensorDataRealm>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .first()
            .find()
        
        return latest?.let {
            SensorData(
                heartRate = it.heartRate,
                oxygenLevel = it.oxygenLevel,
                temperature = it.temperature,
                stressLevel = it.stressLevel,
                batteryLevel = it.batteryLevel,
                timestamp = it.timestamp
            )
        }
    }
    
    // Get sensor data from last N hours
    fun getSensorDataFromLastHours(hours: Int): Flow<List<SensorData>> {
        val cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000)
        
        return realm.query<SensorDataRealm>("timestamp >= $0", cutoffTime)
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .asFlow()
            .map { results ->
                results.list.map { realmObj ->
                    SensorData(
                        heartRate = realmObj.heartRate,
                        oxygenLevel = realmObj.oxygenLevel,
                        temperature = realmObj.temperature,
                        stressLevel = realmObj.stressLevel,
                        batteryLevel = realmObj.batteryLevel,
                        timestamp = realmObj.timestamp
                    )
                }
            }
    }
    
    // Get filtered sensor data
    fun getFilteredSensorData(
        minHeartRate: Int? = null,
        minOxygenLevel: Int? = null,
        maxStressLevel: Int? = null
    ): Flow<List<SensorData>> {
        
        var queryString = "timestamp > 0" // Base query
        val args = mutableListOf<Any>()
        var argIndex = 0
        
        minHeartRate?.let {
            queryString += " AND heartRate >= $${argIndex}"
            args.add(it)
            argIndex++
        }
        
        minOxygenLevel?.let {
            queryString += " AND oxygenLevel >= $${argIndex}"
            args.add(it)
            argIndex++
        }
        
        maxStressLevel?.let {
            queryString += " AND stressLevel <= $${argIndex}"
            args.add(it)
            argIndex++
        }
        
        return realm.query<SensorDataRealm>(queryString, *args.toTypedArray())
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .asFlow()
            .map { results ->
                results.list.map { realmObj ->
                    SensorData(
                        heartRate = realmObj.heartRate,
                        oxygenLevel = realmObj.oxygenLevel,
                        temperature = realmObj.temperature,
                        stressLevel = realmObj.stressLevel,
                        batteryLevel = realmObj.batteryLevel,
                        timestamp = realmObj.timestamp
                    )
                }
            }
    }
    
    // Clean old data (keep only last N days)
    suspend fun cleanOldData(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000)
        
        realm.write {
            val oldData = query<SensorDataRealm>("timestamp < $0", cutoffTime).find()
            delete(oldData)
        }
    }
    
    // Update device connection status
    suspend fun updateConnectionStatus(
        isConnected: Boolean,
        deviceName: String,
        signalStrength: Int
    ) {
        realm.write {
            // Delete old status and create new one (simpler than update)
            val oldStatus = query<ConnectionStatusRealm>().find()
            delete(oldStatus)
            
            val newStatus = ConnectionStatusRealm().apply {
                this.isConnected = isConnected
                this.deviceName = deviceName
                this.signalStrength = signalStrength
                this.lastUpdate = System.currentTimeMillis()
            }
            copyToRealm(newStatus)
        }
    }
    
    // Get current connection status
    fun getConnectionStatusFlow(): Flow<ConnectionStatus?> {
        return realm.query<ConnectionStatusRealm>()
            .asFlow()
            .map { results ->
                results.list.firstOrNull()?.let {
                    ConnectionStatus(
                        isConnected = it.isConnected,
                        deviceName = it.deviceName,
                        signalStrength = it.signalStrength,
                        lastUpdate = it.lastUpdate
                    )
                }
            }
    }
    
    fun close() {
        realm.close()
    }
}