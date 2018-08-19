package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@Transactional
public class Service {
    @Autowired
    private Repository repository;

    public Entity save(Entity entity) {
        return repository.save(entity);
    }

    public Entity get(long id) {
        return repository.findById(id).orElse(new Entity());
    }

    public Iterable<Entity> getAll() {
        return repository.findAll();
    }

    public Entity update(Entity entity) {
        return repository.save(entity);
    }

    public String delete(long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return "ok";
        }

        return "entity with such id does not exist";
    }

    // value is not unique
    public Iterable<Entity> getByValue(String exactValue) {
        return repository.findByValue(exactValue);
    }

    public Iterable<Entity> getTop10() {
        return repository.findTop10ByOrderByIdAsc();
    }

    public Iterable<Entity> getByIdBetween(long min, long max) {
        return repository.findAllByIdBetween(min, max);
    }

    public Iterable<Entity> getByValueIsLike(String pattern) {
        return repository.findAllByValueIsLike(pattern);
    }
}
