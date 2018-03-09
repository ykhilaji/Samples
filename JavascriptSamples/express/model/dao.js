let mongo = require('mongoose');

mongo.connect('mongodb://192.168.99.100:27017/crud');

mongo.connection.on('connected', function () {
    console.log("Connected to MongoDB");
});

mongo.connection.on('error', function (err) {
    console.log("Error: " + err);
});


let schema = new mongo.Schema({
    user: {type: String, required: true, unique: true},
    message: {type: String, required: true},
    priority: {type: Number, 'default': 0, min: 0, max: 3}
});

mongo.model('user', schema, 'users');

module.exports = mongo;