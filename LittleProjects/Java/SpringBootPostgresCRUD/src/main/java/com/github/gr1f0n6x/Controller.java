package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
public class Controller {
    private final EntityRepository repository;

    @Autowired
    public Controller(EntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/api/{id}", produces = "application/json")
    public Entity getEntity(@PathVariable(name = "id") int id) {
        return repository.findById(id).orElse(new Entity());
    }

    @GetMapping(value = "/api", produces = "application/json")
    public Iterable<Entity> getEntities() {
        return repository.findAll();
    }

    @DeleteMapping(value = "/api/{id}")
    public void deleteEntity(@PathVariable(name = "id") int id) {
        repository.deleteById(id);
    }

    @PutMapping(value = "/api", consumes = "application/json", produces = "application/json")
    public Entity createEntity(@RequestBody Entity entity) {
        System.out.println(entity);
        return repository.save(entity);
    }

    @PostMapping(value = "/api",consumes = "application/json", produces = "application/json")
    public Entity updateEntity(@RequestBody Entity entity) {
        System.out.println(entity);
        return repository.save(entity);
    }
}
