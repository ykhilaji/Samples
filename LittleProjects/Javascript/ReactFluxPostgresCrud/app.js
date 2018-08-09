import express from 'express'
import path from 'path'
import bodyParser from 'body-parser'
import api from './core/router/api'
import datasource from './core/datasource/datasource'

const app = express();

app.use(express.static(path.join(__dirname, 'public')));
app.use(bodyParser.json());

app.use((req, res, next) => {
    console.log("Request type: " + req.method);
    next();
});

const index = express.Router();
index.get('/', function (req, res) {
    return res.sendFile(path.join(__dirname, 'public/index.html'));
});

app.use('/', index);
app.use('/api', api);


datasource.query('create table if not exists public.entity (id serial primary key, value varchar(255) not null)')
    .then(data => {
        app.listen(8080);
        console.log('Server started');
    }).catch(error => console.log(error));
