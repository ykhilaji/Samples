import express from 'express'
import path from 'path'
import bodyParser from 'body-parser'
import mongoose from 'mongoose'
import crud from './core/controller/crud'

const server = express();
const index = express.Router();

server.use(express.static(path.join(__dirname, 'public')));
server.use(bodyParser.json());

index.get('/', function (req, res) {
    return res.sendFile(path.join(__dirname, "public/html/index.html"));
});

server.use('/', index);
server.use('/api', crud);

mongoose.connect('mongodb://192.168.99.100:27017/mongocrud');
const db = mongoose.connection;

db.on('error', console.error.bind(console, 'connection error:'));
db.once('open', function() {
    server.listen(8080);
    console.log("Server started");
});