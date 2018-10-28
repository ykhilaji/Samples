package com.github.gr1f0n6x.springbootcassandracrud.controller;

import com.github.gr1f0n6x.springbootcassandracrud.model.Entity;
import com.github.gr1f0n6x.springbootcassandracrud.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WebController {
    private final EntityService service;

    @Autowired
    public WebController(EntityService service) {
        this.service = service;
    }

    @GetMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Entity findById(@RequestBody Entity.Key key) {
        return service.findById(key);
    }

    @GetMapping(value = "/api/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Entity> findAll() {
        return service.findAll();
    }

    @DeleteMapping(value = "/api", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteById(@RequestBody Entity.Key key) {

    }

    @DeleteMapping(value = "/api/all")
    public void deleteAll() {
        service.deleteAll();
    }
    /*
    {
	"key": {
		"id": 1,
		"type": "simple",
		"time": "2018-10-28 21:39:00"
	},
	"value": "value"
}
     */
    @PutMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Entity save(@RequestBody Entity entity) {
        return service.save(entity);
    }

    @PostMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Entity update(@RequestBody Entity entity) {
        return service.update(entity);
    }
}
