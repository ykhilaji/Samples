let express = require('express');
let router = express.Router();
let mongo = require('../model/dao');

let user = mongo.model('user');

router.post('/create', function (req, res) {
    console.log("CREATE: " + req.body);

    user.create(req.body, function (err, result) {
        console.log("trying to create user");
        if (err) {
            console.log(err);
        }

        console.log(result);
        res.json(result);
    });
});

router.get('/read/:username', function (req, res) {
    console.log("READ: " + req.params.username);

    user.findOne({user: req.params.username}, function (err, result) {
        console.log("User: " + result);
        res.json(result);
    });
});

router.get('/read', function (req, res) {
    console.log("READ ALL");

    user.find({})
        .select('user message priority')
        .exec(function (err, result) {
            if (err) {
                console.log(err);
                res.json({"error": err});
            }

            console.log("Result: " + result);
            res.json(result);
        });
});

router.post('/update/:username', function (req, res) {
    console.log("UPDATE: " + req.params.username);

    user.findOneAndUpdate({user: req.params.username}, req.body, function (err, obj) {
        if (err) {
            res.json({'status': 'error'});
        }

        res.json({'status': 'ok', 'user': obj});
    });
});

router.post('/delete/:username', function (req, res) {
    console.log("DELETE: " + req.params.username);

    user.findOneAndRemove({user: req.params.username})
        .exec(function (err) {
            if (err) {
                res.json({'status': 'error'})
            }

            res.json({'status': 'ok'});
        });
});

module.exports = router;