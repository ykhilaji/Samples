package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@Transactional
public class Service {
    private final EntityRepository repository;

    @Autowired
    public Service(EntityRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "entities")
    public Entity getById(Long id) {
        return repository.findById(id).orElse(new Entity());
    }

    @Cacheable(value = "entities")
    public Iterable<Entity> getAll() {
        return repository.findAll();
    }

    @CacheEvict(value = "entities", key = "#id")
    public String deleteById(Long id) {
        try {
            repository.deleteById(id);
            return "ok";
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    @CacheEvict(value = "entities", allEntries = true)
    public String deleteAll() {
        try {
            repository.deleteAll();
            return "ok";
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    @CachePut(value="entities", key = "#entity.id")
    public Entity update(Entity entity) {
        return repository.save(entity);
    }

    @CachePut(value="entities", key = "#entity.id")
    public Entity save(Entity entity) {
        return repository.save(entity);
    }
}
