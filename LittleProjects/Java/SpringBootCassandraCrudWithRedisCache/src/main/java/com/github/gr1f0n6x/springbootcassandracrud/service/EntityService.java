package com.github.gr1f0n6x.springbootcassandracrud.service;

import com.github.gr1f0n6x.springbootcassandracrud.model.Entity;
import com.github.gr1f0n6x.springbootcassandracrud.repository.EntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@org.springframework.stereotype.Service
public class EntityService implements Service<Entity, Entity.Key> {
    private final EntityRepository repository;

    @Autowired
    public EntityService(EntityRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "entity")
    @Override
    public Entity findById(Entity.Key key) {
        return repository.findById(key).orElse(new Entity());
    }

    @Override
    public List<Entity> findAll() {
        return repository.findAll();
    }

    @CacheEvict(cacheNames = "entity")
    @Override
    public void deleteById(Entity.Key key) {
        repository.deleteById(key);
    }

    @CacheEvict(cacheNames = "entity")
    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @CachePut(cacheNames = "entity")
    @Override
    public Entity save(Entity entity) {
        return repository.save(entity);
    }

    @CachePut(cacheNames = "entity")
    @Override
    public Entity update(Entity entity) {
        return repository.save(entity);
    }
}
