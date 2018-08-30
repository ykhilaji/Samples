import express from 'express'
import path from 'path'
import bodyParser from 'body-parser'
import Service from './core/service'

const loggingMiddleware = (req, res, next) => {
    console.log(`Method: ${req.method} Params: ${JSON.stringify(req.params)} Query params: ${JSON.stringify(req.query)}`);
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
const service = new Service();

apiRoute.get('/:id', (req, res) => {
    service.getById(req.params.id).then(entity => res.json(entity)).catch(err => res.send(err));
});

apiRoute.post('/', (req, res) => {
    service.update(req.body).then(entity => res.json(entity)).catch(err => res.send(err));
});

apiRoute.put('/', (req, res) => {
    service.insert(req.body).then(entity => res.json(entity)).catch(err => res.send(err));
});

apiRoute.delete('/', (req, res) => {
    service.deleteById(req.query.id).then(entity => res.json(entity)).catch(err => res.send(err));
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