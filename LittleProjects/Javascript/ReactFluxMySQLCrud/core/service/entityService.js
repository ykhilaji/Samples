import pool from '../configuration/dataSource'
import repository from '../repository/entityRepository'

class EntityService {
    constructor() {
    }
}

EntityService.prototype.findOne = (res) => (id) => {
    pool.getConnection(function (err, connection) {
        if (err) {
            console.log(err);
            return;
        }

        try {
            repository.findOne(connection, res)(id);
        } catch (e) {
            console.log(e);
        } finally {
            connection.release();
        }
    });
};

EntityService.prototype.findAll = (res) => () => {
    pool.getConnection(function (err, connection) {
        if (err) {
            console.log(err);
            return;
        }

        try {
            repository.findAll(connection, res)();
        } catch (e) {
            console.log(e);
        } finally {
            connection.release();
        }
    });
};

EntityService.prototype.save = (res) => (entity) => {
    pool.getConnection(function (err, connection) {
        if (err) {
            console.log(err);
            return;
        }

        try {
            repository.save(connection, res)(entity);
        } catch (e) {
            console.log(e);
        } finally {
            connection.release();
        }
    });

};

EntityService.prototype.deleteById = (res) => (id) => {
    pool.getConnection(function (err, connection) {
        if (err) {
            console.log(err);
            return;
        }

        try {
            repository.deleteById(connection, res)(id);
        } catch (e) {
            console.log(e);
        } finally {
            connection.release();
        }
    });

};

EntityService.prototype.update = (res) => (entity) => {
    pool.getConnection(function (err, connection) {
        if (err) {
            console.log(err);
            return;
        }

        try {
            repository.update(connection, res)(entity);
        } catch (e) {
            console.log(e);
        } finally {
            connection.release();
        }
    });

};

const service = new EntityService();

export default service;