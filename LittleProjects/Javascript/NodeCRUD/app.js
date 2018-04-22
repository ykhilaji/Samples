const path = require('path');
const express = require('express');
const cookieParser = require('cookie-parser');
const bodyParser = require('body-parser');

let bookRouter = require('./core/routes/bookroute');

let app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));
app.set('port', 8080);

app.use('/', bookRouter);

app.listen(app.get('port'));
console.log("Server started");