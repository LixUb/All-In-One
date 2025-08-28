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

async function output(clientName, collectionName, jsonName) {
  try {
    await client.connect();
    const db = client.db(clientName);
    const collection = db.collection(collectionName);
    const students = await collection.find({}).toArray();
    const jsonData = JSON.stringify(students, null, 2);

    fs.writeFileSync(`${jsonName}.json`, jsonData);
    console.log(`The data has been save in "${jsonName}"`)

  } catch (err) {
    console.error("Error:", err);
  } finally {
    await client.close();
  }
}

rl.question("Enter the client name: ", async (clientName) => {
  rl.question("Enter the collection name: ", async (collectionName) => {
    rl.question("Enter the name of exported file: ", async (jsonName) => {
      output(clientName, collectionName, jsonName)
      .catch(console.error)
      .finally(() => rl.close());
    });
  });
});
