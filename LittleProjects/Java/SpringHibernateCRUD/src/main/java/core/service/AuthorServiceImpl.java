package core.service;

import core.model.Author;
import core.repository.AuthorRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@Transactional
public class AuthorServiceImpl implements AuthorService {
    private static Logger logger = Logger.getLogger(AuthorServiceImpl.class.getSimpleName());

    private final AuthorRepository repository;

    @Autowired
    public AuthorServiceImpl(AuthorRepository repository) {
        this.repository = repository;
    }

    public Author select(Long aLong) {
        return repository.select(aLong);
    }

    public Iterable<Author> select() {
        return repository.select();
    }

    public Author insert(Author entity) {
        return repository.insert(entity);
    }

    public Iterable<Author> insert(Iterable<Author> entities) {
        return repository.insert(entities);
    }

    public Author update(Author entity) {
        return repository.update(entity);
    }

    public Iterable<Author> update(Iterable<Author> entities) {

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
    public void delete(Author entity) {
        repository.delete(entity);
    }

    @Override
    public void delete(Iterable<Author> entities) {
        repository.delete(entities);
    }
}
