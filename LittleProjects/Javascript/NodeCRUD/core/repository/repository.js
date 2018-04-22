const pool = require('../datasource/pgsource');

class Repository {
    constructor(schema, table, entity) {
        this.schema = schema;
        this.table = table;
        this.entity = entity;
    }

    save(entity) {
        return pool.query(`insert into ${this.schema}.${this.table}(${this.entity.fields().slice(1).map(field => `"${field}"`).join()}) values(${this.entity.fields().slice(1).map((field, index) => `$${index + 1}`)}) returning "${this.entity.fields()[0]}"`,
            entity.values().slice(1))
            .then(result => {
                entity.setId(result.rows[0][this.entity.fields()[0]]);
                return entity;
            })
            .catch(err => JSON.stringify(err));
    }

    get(id) {
        return pool.query(`select ${this.entity.fields().join()} from ${this.schema}.${this.table} where ${this.entity.fields()[0]} = $1`, [id])
            .then(result => this.entity.apply(result.rows))
            .catch(err => JSON.stringify(err));
    }

    remove(id) {
        return pool.query(`delete from ${this.schema}.${this.table} where ${this.entity.fields()[0]} = $1`, [id])
            .then(_ =>  JSON.stringify({"status": "ok"}))
            .catch(err =>  JSON.stringify(err))
    }

    update(entity) {
        console.log(`update ${this.schema}.${this.table} set ${this.entity.fields().slice(1).map((field, index) => `${field} = $${index + 2}`)} where ${this.entity.fields()[0]} = $1`);
        return pool.query(`update ${this.schema}.${this.table} set ${this.entity.fields().slice(1).map((field, index) => `${field} = $${index + 2}`)} where ${this.entity.fields()[0]} = $1`,
            entity.values())
            .then(_ =>  JSON.stringify({"status": "ok"}))
            .catch(err => JSON.stringify(err))
    }
}

module.exports = Repository;