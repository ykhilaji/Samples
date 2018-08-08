package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AppController {
    private final EntityRepository repository;

    @Autowired
    public AppController(EntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/api/{id}", produces = "application/json")
    public Entity get(@PathVariable("id") int id) {
        return repository.findById(id).orElse(new Entity());
    }

    @GetMapping(value = "/api", produces = "application/json")
    public Iterable<Entity> getAll() {
        return repository.findAll();
    }

    @DeleteMapping(value = "/api/{id}", produces = "text/plain")
    public String delete(@PathVariable("id") int id) {
        try {
            repository.deleteById(id);
            return "ok";
        } catch (Exception e) {
            return "error";
        }
    }

    @PutMapping(value = "/api", consumes = "application/json", produces = "application/json")
    public Entity create(@RequestBody Entity entity) {
        return repository.save(entity);
    }

    @PostMapping(value = "/api", consumes = "application/json", produces = "application/json")
    public Entity update(@RequestBody Entity entity) {
        return repository.save(entity);
    }
}
