class Service {
    constructor(repository) {
        this.repository = repository;
    }

    get(id) {
        return this.repository.get(id);
    }

    save(object) {
        return this.repository.save(object)
    }

    remove(id) {
        return this.repository.remove(id);
    }

    update(object) {
        return this.repository.update(object);
    }
}

module.exports = Service;