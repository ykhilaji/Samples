package core.controller;

import core.model.Book;
import core.service.BookService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/book")
public class BookController {
    private static Logger logger = Logger.getLogger(BookController.class.getSimpleName());
    private final BookService service;

    @Autowired
    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping(value = "/select/{id}", produces = "application/json")
    public Book bookById(@PathVariable Long id) {
        return service.select(id);
    }

    @GetMapping(value = "/select", produces = "application/json")
    public Iterable<Book> allBooks() {
        return service.select();
    }

    @DeleteMapping(value = "/delete/{id}")
    public void deleteBookById(@PathVariable Long id) {
        service.deleteById(id);
    }

    @DeleteMapping(value = "/delete")
    public void deleteBooksByIds(@RequestBody Iterable<Long> ids) {
        service.deleteById(ids);
    }

    @PutMapping(value = "/save", produces = "application/json")
    public Book saveBook(@RequestBody Book book) {
        return service.insert(book);
    }

    @PutMapping(value = "/save/all", produces = "application/json")
    public Iterable<Book> saveBooks(@RequestBody Iterable<Book> books) {
        return service.insert(books);
    }

    @PostMapping(value = "/update", produces = "application/json")
    public Book updateBook(@RequestBody Book book) {
        return service.update(book);
    }

    @PostMapping(value = "/update/all", produces = "application/json")
    public Iterable<Book> updateBooks(@RequestBody Iterable<Book> books) {
        return service.update(books);
    }
}
