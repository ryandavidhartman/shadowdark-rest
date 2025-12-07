// Seed the Backgrounds collection from the BFRPG table.
// Run with: mongosh --file data/seed-backgrounds.js "mongodb://localhost:27017/shadowdark"

/* global Mongo, ObjectId */
const fs = require('fs');

const positionalArgs = process.argv.slice(2).filter(a => !a.startsWith('--'));
const uriArg = positionalArgs.find(a => a.startsWith('mongodb'));
const dbArg = positionalArgs.find(a => a.startsWith('db='));
const collectionArg = positionalArgs.find(a => a.startsWith('collection='));

const uri = uriArg || process.env.MONGO_URI || 'mongodb://localhost:27017/shadowdark';

const parsed = new URL(uri);
const dbName =
  (dbArg && dbArg.split('=')[1]) ||
  process.env.MONGO_DB ||
  (parsed.pathname && parsed.pathname !== '/' ? parsed.pathname.slice(1) : 'shadowdark');
const collectionName =
  (collectionArg && collectionArg.split('=')[1]) ||
  process.env.MONGO_COLLECTION ||
  'Backgrounds';

const backgrounds = JSON.parse(fs.readFileSync('data/backgrounds.json', 'utf8'));

const conn = new Mongo(uri);
const db = conn.getDB(dbName);
const collection = db.getCollection(collectionName);

print(`Seeding ${collectionName} in ${dbName} at ${uri}`);
collection.deleteMany({});
collection.insertMany(backgrounds.map(bg => ({ _id: new ObjectId(), ...bg })));

print('Done.');
