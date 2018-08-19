package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {
    @Autowired
    private Service service;

    @GetMapping(value = "/api")
    public Entity get(@RequestParam(name = "id", required = true) long id) {
        return service.get(id);
    }

    @GetMapping(value = "/api/all")
    public Iterable<Entity> getAll() {
        return service.getAll();
    }

    @PutMapping(value = "/api")
    public Entity save(@RequestBody Entity entity) {
        return service.save(entity);
    }

    @PostMapping(value = "/api")
    public Entity update(@RequestBody Entity entity) {
        return service.update(entity);
    }

    @DeleteMapping(value = "/api/{id}")
    public String delete(@PathVariable(name = "id") long id) {
        return service.delete(id);
    }

    @GetMapping(value = "/api/top10")
    public Iterable<Entity> getTop10() {
        return service.getTop10();
    }

    //localhost:8080/api/between?min=0&max=1
    @GetMapping(value = "/api/between")
    public Iterable<Entity> getByIdBetween(@RequestParam(name = "min") long min, @RequestParam(name = "max") long max) {
        return service.getByIdBetween(min, max);
    }

    //localhost:8080/api/value?value=u%25&exact=false
    @GetMapping(value = "/api/value")
    public Iterable<Entity> getByValue(@RequestParam(name = "value") String value, @RequestParam(name = "exact", required = false, defaultValue = "false") boolean exact) {
        if (exact) {
            return service.getByValue(value);
        } else {
            return service.getByValueIsLike(value);
        }
    }
}
