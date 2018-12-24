class EntityRepository {
    constructor() {
    }
}

// strange construction -> (connection, res)
EntityRepository.prototype.findOne = (connection, res) => (id) => {
    connection.query('SELECT * FROM `crud`.`entity` WHERE `id` = ?', [id], function (error, results, fields) {
        if (error) {
            console.log(error);
            res.status(500).send(error.message);
            return
        }
        console.log(`Results: ${JSON.stringify(results)}`);
        res.json(results);
    });
};

EntityRepository.prototype.findAll = (connection, res) => () => {
    connection.query('SELECT * FROM `crud`.`entity`', function (error, results, fields) {
        if (error) {
            console.log(error);
            res.status(500).send(error.message);
            return
        }
        console.log(`Results: ${JSON.stringify(results)}`);
        res.json(results);
    });
};

EntityRepository.prototype.save = (connection, res) => (entity) => {
    connection.query('insert into `crud`.`entity`(`id`, `value`) values (?, ?)', [entity.id, entity.value], function (error, results, fields) {
        if (error) {
            console.log(error);
            res.status(500).send(error.message);
            return
        }
        console.log(`Results: ${JSON.stringify(results)}`);
        res.json(results);
    });
};

EntityRepository.prototype.deleteById = (connection, res) => (id) => {
    connection.query('delete from `crud`.`entity` where `id` = ?', [id], function (error, results, fields) {
        if (error) {
            console.log(error);
            res.status(500).send(error.message);
            return
        }
        console.log(`Results: ${JSON.stringify(results)}`);
        res.json(results);
    });
};

EntityRepository.prototype.update = (connection, res) => (entity) => {
    connection.query('update `crud`.`entity` set `value` = ? where `id` = ?', [entity.value, entity.id], function (error, results, fields) {
        if (error) {
            console.log(error);
            res.status(500).send(error.message);
            return
        }
        console.log(`Results: ${JSON.stringify(results)}`);
        res.json(results);
    });
};

const repository = new EntityRepository();

export default repository;