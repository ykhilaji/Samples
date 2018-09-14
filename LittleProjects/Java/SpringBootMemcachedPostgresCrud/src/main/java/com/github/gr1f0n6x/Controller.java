package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {
    private final Service service;

    @Autowired
    public Controller(Service service) {
        this.service = service;
    }

    @GetMapping(value = "/api/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Entity getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @GetMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Entity> getAll() {

        return service.getAll();
    }

    @DeleteMapping(value = "/api/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String deleteById(@PathVariable("id") Long id) {
        return service.deleteById(id);
    }

    @DeleteMapping(value = "/api", produces = MediaType.TEXT_PLAIN_VALUE)
    public String deleteAll() {
        return service.deleteAll();
    }

    @PutMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Entity save(@RequestBody Entity entity) {
        return service.save(entity);
    }

    @PostMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Entity update(@RequestBody Entity entity) {
        return service.update(entity);
    }
}
