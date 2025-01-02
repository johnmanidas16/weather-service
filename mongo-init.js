// mongo-init.js
db = db.getSiblingDB('weatherdb');

db.createCollection('users');
db.createCollection('weatherData');

// You can add any initial data or indexes here if needed