package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.PostConstruct;

@org.springframework.stereotype.Service
public class Service {
    @Autowired
    private Repository repository;
    @Autowired
    private RedisTemplate redisTemplate;
    private ValueOperations valueOperations;

    @PostConstruct
    public void postConstruct() {
        this.valueOperations = this.redisTemplate.opsForValue();
    }

    public Entity get(long id) {
        return repository.findById(id).orElse(new Entity());
    }

    public Iterable<Entity> getAll() {
        return repository.findAll();
    }

    public Entity save(Entity entity) {
        long nextId = valueOperations.increment("id", 1);

        entity.setId((int) nextId);
        return repository.save(entity);
    }

    public Entity update(Entity entity) {
        return repository.save(entity);
    }

    public String delete(long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return "deleted";
        }

        return "Entity with such id does not exist";
    }
}
