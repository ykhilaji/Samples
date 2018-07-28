let path = require('path');
let express = require('express');
let cookieParser = require('cookie-parser');
let bodyParser = require('body-parser');


let router = express.Router();
router.get('/', function (req, res) {
    res.sendfile('index.html');
});

let app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, '')));

app.use('/', router);

app.listen(8080);
console.log("Server started");