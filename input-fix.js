require("dotenv").config();
const { MongoClient } = require("mongodb");
const fs = require("fs");
const readline = require("readline");

const url = process.env.MONGO_URL;
const client = new MongoClient(url);

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

async function insertStudent(collectionName, clientName) {
  try {
    await client.connect();
    const db = client.db(clientName);
    const collection = db.collection(collectionName);

    rl.question("Name: ", (name) => {
      rl.question("Age: ", (age) => {
        rl.question("Class: ", (className) => {
          rl.question("Grade: ", async (grade) => {
            const result = await collection.insertOne({
              name,
              age: parseInt(age),
              className,
              grade
            });

            console.log(`Data inserted with _id: ${result.insertedId}`);
            await client.close();
            rl.close();
          });
        });
      });
    });
  } catch (err) {
    console.error("Error:", err);
  }
}

async function importData(filePath, collectionName, clientName) {
  try {
    await client.connect();
    const db = client.db(clientName);
    const collection = db.collection(collectionName);

    const data = JSON.parse(fs.readFileSync(filePath, "utf8"));
    const cleanData = data.map(({ _id, ...rest }) => rest);
    await collection.insertMany(cleanData);

    console.log(`Data imported from ${filePath} into client '${clientName}' & collection '${collectionName}'`);
  } catch (err) {
    console.error("Error:", err);
  } finally {
    await client.close();
  }
}

rl.question("Choose an option:\n1. Insert single student\n2. Import from JSON file\n> ", (choice) => {
  if (choice === "1") {
    rl.question("Enter the collection name: ", (collectionName) => {
      rl.question("Enter the client name: ", (clientName) => {
        insertStudent(collectionName, clientName)
            .catch(console.error)
            .finally(() => rl.close());
    });
  });
  } else if (choice === "2") {
    rl.question("Enter the JSON file path: ", (filePath) => {
      rl.question("Enter the client name: ", (clientName) => {
        rl.question("Enter the collection name: ", (collectionName) => {
          importData(filePath, collectionName, clientName)
            .catch(console.error)
            .finally(() => rl.close());
        });
      });
    });
  } else {
    console.log("Invalid choice. Exiting.");
    rl.close();
  }
});
