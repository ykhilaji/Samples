import express from 'express'
import path from 'path'
import bodyParser from 'body-parser'
import service from './core/service/entityService'

const loggingMiddleware = (req, res, next) => {
    console.log(`Method: ${req.method} Params: ${JSON.stringify(req.params)} Query params: ${JSON.stringify(req.query)} Body ${JSON.stringify(req.body)}`);
    next();
};

const errorHandler = (err, req, res, next) => {
    if (err) {
        console.log(err.stack);
        res.status(500).send(err.message);
    } else {
        next();
    }
};

const indexRoute = express.Router();
indexRoute.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public/index.html'));
});

const apiRoute = express.Router();

apiRoute.get('/', (req, res) => {
    service.findAll(res)();
});

apiRoute.get('/:id', (req, res) => {
    service.findOne(res)(req.params.id);
});

apiRoute.post('/', (req, res) => {
    service.update(res)(req.body);
});

apiRoute.put('/', (req, res) => {
    service.save(res)(req.body);
});

apiRoute.delete('/:id', (req, res) => {
    service.deleteById(res)(req.params.id);
});

const server = express();
server.use(express.static(path.join(__dirname, 'public')));
server.use(bodyParser.json());
server.use(errorHandler);
server.use(loggingMiddleware);
server.use('/', indexRoute);
server.use('/api', apiRoute);

server.listen(8080);
console.log('Server started');