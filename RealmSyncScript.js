require("dotenv").config();
const { MongoClient } = require("mongodb");
const Realm = require("realm");

// MongoDB setup
const mongoUrl = process.env.MONGO_URL;
const mongoClient = new MongoClient(mongoUrl);

// Realm Schema definitions
const SensorDataSchema = {
  name: 'SensorData',
  primaryKey: '_id',
  properties: {
    _id: 'objectId',
    heartRate: { type: 'int', default: 0 },
    oxygenLevel: { type: 'int', default: 0 },
    temperature: { type: 'float', default: 0.0 },
    stressLevel: { type: 'int', default: 0 },
    batteryLevel: { type: 'int', default: 0 },
    timestamp: { type: 'int', default: () => Date.now() }
  }
};

const ConnectionStatusSchema = {
  name: 'ConnectionStatus',
  primaryKey: '_id',
  properties: {
    _id: 'objectId',
    isConnected: { type: 'bool', default: false },
    deviceName: { type: 'string', default: '' },
    signalStrength: { type: 'int', default: 0 },
    lastUpdate: { type: 'int', default: 0 }
  }
};

const DeviceInfoSchema = {
  name: 'DeviceInfo',
  primaryKey: '_id',
  properties: {
    _id: 'objectId',
    name: { type: 'string', default: 'KAVLO_SmartHeadband' },
    macAddress: { type: 'string', default: '' },
    firmwareVersion: { type: 'string', default: '1.0.0' },
    batteryLevel: { type: 'int', default: 0 }
  }
};

class MongoToRealmSync {
  constructor() {
    this.realm = null;
    this.mongoDB = null;
  }

  async initialize() {
    try {
      // Connect to MongoDB
      await mongoClient.connect();
      this.mongoDB = mongoClient.db(process.env.MONGO_DB_NAME || "sensordb");
      console.log("Connected to MongoDB");

      // Open Realm
      this.realm = await Realm.open({
        schema: [SensorDataSchema, ConnectionStatusSchema, DeviceInfoSchema],
        schemaVersion: 1
      });
      console.log("Connected to Realm");

    } catch (error) {
      console.error("Initialization error:", error);
      throw error;
    }
  }

  // One-time migration from MongoDB to Realm
  async migrateAllData() {
    try {
      const collection = this.mongoDB.collection(process.env.MONGO_COLLECTION || "sensors");
      const mongoData = await collection.find({}).toArray();
      
      console.log(`Found ${mongoData.length} records in MongoDB`);

      this.realm.write(() => {
        // Clear existing data
        this.realm.delete(this.realm.objects('SensorData'));
        
        // Insert MongoDB data
        mongoData.forEach(doc => {
          this.realm.create('SensorData', {
            _id: new Realm.BSON.ObjectId(),
            heartRate: doc.heartRate || 0,
            oxygenLevel: doc.oxygenLevel || 0,
            temperature: parseFloat(doc.temperature) || 0.0,
            stressLevel: doc.stressLevel || 0,
            batteryLevel: doc.batteryLevel || 0,
            timestamp: doc.timestamp || Date.now()
          });
        });
      });

      console.log(`Migrated ${mongoData.length} records to Realm`);
      
    } catch (error) {
      console.error("Migration error:", error);
      throw error;
    }
  }

  // Real-time sync: Watch MongoDB changes and update Realm
  async startRealTimeSync() {
    try {
      const collection = this.mongoDB.collection(process.env.MONGO_COLLECTION || "sensors");
      
      console.log("Starting real-time sync...");
      
      // Watch for changes in MongoDB
      const changeStream = collection.watch();
      
      changeStream.on('change', (change) => {
        console.log('MongoDB change detected:', change.operationType);
        
        this.realm.write(() => {
          switch (change.operationType) {
            case 'insert':
              const doc = change.fullDocument;
              this.realm.create('SensorData', {
                _id: new Realm.BSON.ObjectId(),
                heartRate: doc.heartRate || 0,
                oxygenLevel: doc.oxygenLevel || 0,
                temperature: parseFloat(doc.temperature) || 0.0,
                stressLevel: doc.stressLevel || 0,
                batteryLevel: doc.batteryLevel || 0,
                timestamp: doc.timestamp || Date.now()
              });
              console.log("Added new record to Realm");
              break;
              
            case 'update':
              // Handle updates if needed
              console.log("Update operation detected");
              break;
              
            case 'delete':
              // Handle deletes if needed
              console.log("Delete operation detected");
              break;
          }
        });
      });

      changeStream.on('error', (error) => {
        console.error('Change stream error:', error);
      });

    } catch (error) {
      console.error("Real-time sync error:", error);
      throw error;
    }
  }

  // Sync latest data only (more efficient)
  async syncLatestData(hoursBack = 1) {
    try {
      const cutoffTime = Date.now() - (hoursBack * 60 * 60 * 1000);
      const collection = this.mongoDB.collection(process.env.MONGO_COLLECTION || "sensors");
      
      const recentData = await collection.find({
        timestamp: { $gte: cutoffTime }
      }).toArray();

      console.log(`Found ${recentData.length} recent records`);

      this.realm.write(() => {
        recentData.forEach(doc => {
          // Check if record already exists
          const existing = this.realm.objects('SensorData').filtered(`timestamp == ${doc.timestamp}`);
          
          if (existing.length === 0) {
            this.realm.create('SensorData', {
              _id: new Realm.BSON.ObjectId(),
              heartRate: doc.heartRate || 0,
              oxygenLevel: doc.oxygenLevel || 0,
              temperature: parseFloat(doc.temperature) || 0.0,
              stressLevel: doc.stressLevel || 0,
              batteryLevel: doc.batteryLevel || 0,
              timestamp: doc.timestamp || Date.now()
            });
          }
        });
      });

      console.log("Latest data synced to Realm");
      
    } catch (error) {
      console.error("Latest sync error:", error);
      throw error;
    }
  }

  // Get data directly from Realm (no API needed)
  getRealmSensorData() {
    const sensorData = this.realm.objects('SensorData').sorted('timestamp', true);
    return Array.from(sensorData).map(obj => ({
      heartRate: obj.heartRate,
      oxygenLevel: obj.oxygenLevel,
      temperature: obj.temperature,
      stressLevel: obj.stressLevel,
      batteryLevel: obj.batteryLevel,
      timestamp: obj.timestamp
    }));
  }

  async cleanup() {
    if (this.realm) {
      this.realm.close();
    }
    if (mongoClient) {
      await mongoClient.close();
    }
    console.log("Connections closed");
  }
}

// Usage examples
async function main() {
  const sync = new MongoToRealmSync();
  
  try {
    await sync.initialize();
    
    // Choose one of these approaches:
    
    // 1. One-time migration
    // await sync.migrateAllData();
    
    // 2. Sync only recent data
    await sync.syncLatestData(24); // Last 24 hours
    
    // 3. Start real-time sync (keeps running)
    // await sync.startRealTimeSync();
    
    // Get data from Realm (this is what your Kotlin app would do)
    const realmData = sync.getRealmSensorData();
    console.log("Sample Realm data:", realmData.slice(0, 3));
    
  } catch (error) {
    console.error("Main error:", error);
  } finally {
    await sync.cleanup();
  }
}

// Run if this file is executed directly
if (require.main === module) {
  main().catch(console.error);
}

module.exports = MongoToRealmSync;