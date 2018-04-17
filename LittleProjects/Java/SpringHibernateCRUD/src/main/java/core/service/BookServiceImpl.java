package core.service;

import core.model.Book;
import core.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@Transactional
public class BookServiceImpl implements BookService {
    private final BookRepository repository;

    @Autowired
    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    public Book select(Long aLong) {
        return repository.select(aLong);
    }

    public Iterable<Book> select() {
        return repository.select();
    }

    public Book insert(Book entity) {
        return repository.insert(entity);
    }

    public Iterable<Book> insert(Iterable<Book> entities) {
        return repository.insert(entities);
    }

    public Book update(Book entity) {
        return repository.update(entity);
    }

    public Iterable<Book> update(Iterable<Book> entities) {
        return repository.update(entities);
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }

    @Override
    public void deleteById(Iterable<Long> longs) {
        repository.deleteById(longs);
    }

    @Override
    public void delete(Book entity) {
        repository.delete(entity);
    }

    @Override
    public void delete(Iterable<Book> entities) {
        repository.delete(entities);
    }
}
