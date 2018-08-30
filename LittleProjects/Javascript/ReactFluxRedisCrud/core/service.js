import Repository from './repository'

class Service {
    constructor() {
        this.repository = new Repository();
    }

    getById(id) {
        return this.repository.getById(id);
    }

    insert(entity) {
        return this.repository.insert(entity).then(res => {
            return {...entity, id: res[0]}
        });
    }

    update(entity) {
        return this.repository.update(entity).then(res => {
            return entity;
        });
    }

    deleteById(id) {
        return this.repository.deleteById(id).then(res => {
            return {id: id};
        });
    }
}

export default Service;