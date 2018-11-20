package com.github.gr1f0n6x.springbootignitecrud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class WebController {
    private final EntityRepository repository;

    @Autowired
    public WebController(EntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/api/get/{id}", produces = "application/json")
    public Entity find(@PathVariable("id") Long id) {
        return repository.findOne(id);
    }

    @PutMapping(value = "/api/save", produces = "application/json", consumes = "application/json")
    public Entity save(@RequestBody Entity entity) {
        return repository.save(entity.getId(), entity);
    }

    @PostMapping(value = "/api/update", produces = "application/json", consumes = "application/json")
    public Entity update(@RequestBody Entity entity) {
        return repository.save(entity.getId(), entity);
    }

    @DeleteMapping(value = "/api/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        repository.delete(id);
    }
}
