package core.service;

import core.model.Publisher;
import core.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@Transactional
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository repository;

    @Autowired
    public PublisherServiceImpl(PublisherRepository repository) {
        this.repository = repository;
    }

    public Publisher select(Long aLong) {
        return repository.select(aLong);
    }

    public Iterable<Publisher> select() {
        return repository.select();
    }

    public Publisher insert(Publisher entity) {
        return repository.insert(entity);
    }

    public Iterable<Publisher> insert(Iterable<Publisher> entities) {
        return repository.insert(entities);
    }

    public Publisher update(Publisher entity) {
        return repository.update(entity);
    }

    public Iterable<Publisher> update(Iterable<Publisher> entities) {
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
    public void delete(Publisher entity) {
        repository.delete(entity);
    }

    @Override
    public void delete(Iterable<Publisher> entities) {
        repository.delete(entities);
    }
}
