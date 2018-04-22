const express = require('express');
const router = express.Router();
const BookService = require('../service/bookservice');

let service = new BookService();

router.get('/:id', function (req, res) {
    service.get(req.params.id)
        .then(entity => res.json(entity))
        .catch(err => res.json(err));
});

router.post('/', function (req, res) {
    service.update(req.body)
        .then(entity => res.json(entity))
        .catch(err => res.json(err));
});

router.put('/', function (req, res) {
    service.save(req.body)
        .then(entity => res.json(entity))
        .catch(err => res.json(err));
});

router.delete('/:id', function (req, res) {
    service.remove(req.params.id)
        .then(result => res.json(result))
        .catch(err => res.json(err));
});


module.exports = router;