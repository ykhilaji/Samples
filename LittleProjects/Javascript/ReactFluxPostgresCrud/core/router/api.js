import express from 'express'
import repository from './../repository/entityrepository'

const api = express.Router();

api.get("/:id", (req, res) => {
    repository.getById(req.params.id)
        .then(data => {
            res.json(data);
        }).catch(error => {
        res.json(error);
    })
});

api.get("/", (req, res) => {
    repository.getAll()
        .then(data => {
            res.json(data);
        }).catch(error => {
        res.json(error);
    })
});

api.delete("/:id", (req, res) => {
    repository.deleteById(req.params.id)
        .then(data => {
            res.json(data);
        }).catch(error => {
        res.json(error);
    })
});

api.post("/", (req, res) => {
    repository.update(req.body)
        .then(data => {
            res.json(data);
        }).catch(error => {
        res.json(error);
    })
});

api.put("/", (req, res) => {
    repository.create(req.body)
        .then(data => {
            res.json(data);
        }).catch(error => {
        res.json(error);
    })
});

export default api;