import datasource from './../datasource/datasource'

function EntityRepository() {
}

EntityRepository.prototype.getById = (id) => {
    return datasource.query('select id, value from public.entity where id = $1', [id])
        .then(data => data.rows);
};

EntityRepository.prototype.getAll = () => {
    return datasource.query('select id, value from public.entity')
        .then(data => data.rows);
};

EntityRepository.prototype.deleteById = (id) => {
    return datasource.connect().then(client => {
        return client.query('begin')
            .then(d => {
                return client.query('delete from public.entity where id = $1', [id])
                    .then(d => {
                        return client.query('commit').then(() => {
                            return {id: id};
                        });
                    })
            }).catch(error => {
                return client.query('rollback').finally(() => {
                    return Promise.reject(error);
                });
            }).finally(() => client.release());
    });
};

EntityRepository.prototype.update = (entity) => {
    return datasource.connect().then(client => {
        return client.query('begin')
            .then(d => {
                return client.query('update public.entity set value = $2 where id = $1', [entity.id, entity.value])
                    .then(d => {
                        return client.query('commit').then(() => {
                            return entity;
                        });
                    })
            }).catch(error => {
                return client.query('rollback').finally(() => {
                    return Promise.reject(error);
                });
            }).finally(() => client.release());
    });
};

EntityRepository.prototype.create = (entity) => {
    return datasource.connect().then(client => {
        return client.query('begin')
            .then(d => {
                return client.query('insert into public.entity(value) values ($1) returning id', [entity.value])
                    .then(d => {
                        return client.query('commit').then(() => {
                            return {
                                ...entity,
                                id: d.rows[0].id
                            }
                        });
                    })
            }).catch(error => {
                return client.query('rollback').finally(() => {
                    return Promise.reject(error);
                });
            }).finally(() => client.release());
    });
};

const repository = new EntityRepository();

export default repository;