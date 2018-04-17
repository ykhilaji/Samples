package core.controller;

import core.model.Author;
import core.service.AuthorService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/author")
public class AuthorController {
    private static Logger logger = Logger.getLogger(AuthorController.class.getSimpleName());
    private final AuthorService service;

    @Autowired
    public AuthorController(AuthorService service) {
        this.service = service;
    }

    @GetMapping(value = "/select/{id}", produces = "application/json")
    public Author authorById(@PathVariable Long id) {
        return service.select(id);
    }

    @GetMapping(value = "/select", produces = "application/json")
    public Iterable<Author> allAuthors() {
        return service.select();
    }

    @DeleteMapping(value = "/delete/{id}")
    public void deleteAuthorById(@PathVariable Long id) {
        service.deleteById(id);
    }

    @DeleteMapping(value = "/delete")
    public void deleteAuthorsByIds(@RequestBody Iterable<Long> ids) {
        service.deleteById(ids);
    }

    @PutMapping(value = "/save", produces = "application/json")
    public Author saveAuthor(@RequestBody Author author) {
        return service.insert(author);
    }

    @PutMapping(value = "/save/all", produces = "application/json")
    public Iterable<Author> saveAuthors(@RequestBody Iterable<Author> authors) {
        return service.insert(authors);
    }

    @PostMapping(value = "/update", produces = "application/json")
    public Author updateAuthor(@RequestBody Author author) {
        return service.update(author);
    }

    @PostMapping(value = "/update/all", produces = "application/json")
    public Iterable<Author> updateAuthors(@RequestBody Iterable<Author> authors) {
        return service.update(authors);
    }
}
