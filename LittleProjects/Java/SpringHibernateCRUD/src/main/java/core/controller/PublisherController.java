package core.controller;

import core.model.Publisher;
import core.service.PublisherService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/publisher")
public class PublisherController {
    private static Logger logger = Logger.getLogger(PublisherController.class.getSimpleName());
    private final PublisherService service;

    @Autowired
    public PublisherController(PublisherService service) {
        this.service = service;
    }

    @GetMapping(value = "/select/{id}", produces = "application/json")
    public Publisher publisherById(@PathVariable Long id) {
        return service.select(id);
    }

    @GetMapping(value = "/select", produces = "application/json")
    public Iterable<Publisher> allPublishers() {
        return service.select();
    }

    @DeleteMapping(value = "/delete/{id}")
    public void deletePublisherById(@PathVariable Long id) {
        service.deleteById(id);
    }

    @DeleteMapping(value = "/delete")
    public void deletePublishersByIds(@RequestBody Iterable<Long> ids) {
        service.deleteById(ids);
    }

    @PutMapping(value = "/save", produces = "application/json")
    public Publisher savePublisher(@RequestBody Publisher publisher) {
        return service.insert(publisher);
    }

    @PutMapping(value = "/save/all", produces = "application/json")
    public Iterable<Publisher> savePublishers(@RequestBody Iterable<Publisher> publishers) {
        return service.insert(publishers);
    }

    @PostMapping(value = "/update", produces = "application/json")
    public Publisher updatePublisher(@RequestBody Publisher publisher) {
        return service.update(publisher);
    }

    @PostMapping(value = "/update/all", produces = "application/json")
    public Iterable<Publisher> updatePublishers(@RequestBody Iterable<Publisher> publishers) {
        return service.update(publishers);
    }
}
