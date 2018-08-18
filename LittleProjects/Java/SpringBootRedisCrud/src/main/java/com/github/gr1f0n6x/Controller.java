package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {
    @Autowired
    private Service service;

    @GetMapping(value = "/api/{id}", produces = "application/json")
    public Entity get(@PathVariable("id") Long id) {
        return service.get(id);
    }

    @GetMapping(value = "/api/all", produces = "application/json")
    public Iterable<Entity> getAll() {
        return service.getAll();
    }

    @PostMapping(value = "/api", consumes = "application/json", produces = "application/json")
    public Entity update(@RequestBody Entity entity) {
        return service.update(entity);
    }

    @PutMapping(value = "/api", consumes = "application/json", produces = "application/json")
    public Entity save(@RequestBody Entity entity) {
        return service.save(entity);
    }

    @DeleteMapping(value = "/api", consumes = "application/json", produces = "application/json")
    public String delete(@RequestBody Long id) {
        return service.delete(id);
    }
}
