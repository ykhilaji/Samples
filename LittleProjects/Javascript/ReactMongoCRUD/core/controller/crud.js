import express from 'express'
import TodoService from './../service/todoService'

const router = express.Router();
const todoService = new TodoService();

router.get('/todo/:id', (req, res) => {
    todoService.getById(req.params.id)
        .then(todo => {
            res.json(todo);
        }).catch(err => {
            res.json(err);
        })
});

router.get('/todo', (req, res) => {
    todoService.getByType(req.query.type)
        .then(todo => {
            res.json(todo);
        }).catch(err => {
            res.json(err);
        })
});

router.post('/todo', (req, res) => {
    let {id, type, body} = req.body;

    todoService.update(id, type, body)
        .then(todo => {
            res.json(todo);
        }).catch(err => {
            res.json(err);
        })
});

router.delete('/todo', (req, res) => {
    let {id} = req.body;

    todoService.deleteById(id)
        .then(result => {
            res.json(result);
        }).catch(err => {
            res.json(err);
        })
});

router.put('/todo', (req, res) => {
    let {type, body} = req.body;

    todoService.save(type, body)
        .then(todo => {
            res.json(todo);
        }).catch(err => {
            res.json(err);
        })
});

export default router;