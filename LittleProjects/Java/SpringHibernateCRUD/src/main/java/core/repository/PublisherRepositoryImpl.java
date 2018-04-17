package core.repository;

import core.model.Publisher;
import core.model.Publisher_;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Component
public class PublisherRepositoryImpl implements PublisherRepository {
    private static Logger logger = Logger.getLogger(PublisherRepositoryImpl.class.getSimpleName());
    @PersistenceContext
    private EntityManager entityManager;

    public Publisher select(Long aLong) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Publisher> criteriaQuery = criteriaBuilder.createQuery(Publisher.class);
        Root<Publisher> root = criteriaQuery.from(Publisher.class);

        criteriaQuery
                .select(root)
                .where(criteriaBuilder.equal(root.get(Publisher_.id), aLong));

        List<Publisher> publishers = entityManager.createQuery(criteriaQuery).getResultList();

        if (publishers.isEmpty()) {
            return new Publisher();
        } else {
            return publishers.get(0);
        }
    }

    public Iterable<Publisher> select() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Publisher> criteriaQuery = criteriaBuilder.createQuery(Publisher.class);
        Root<Publisher> root = criteriaQuery.from(Publisher.class);

        criteriaQuery.select(root);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public Publisher insert(Publisher entity) {
        entityManager.persist(entity);

        return entity;
    }

    public Iterable<Publisher> insert(Iterable<Publisher> entities) {
        entities.forEach(entityManager::persist);

        return entities;
    }

    public Publisher update(Publisher entity) {
        Publisher publisher = entityManager.merge(entity);

        return publisher;
    }

    public Iterable<Publisher> update(Iterable<Publisher> entities) {
        List<Publisher> publishers = new ArrayList<>();

        entities.forEach(a -> publishers.add(entityManager.merge(a)));

        return publishers;
    }

    @Override
    public void deleteById(Long aLong) {
        entityManager.remove(select(aLong));
    }

    @Override
    public void deleteById(Iterable<Long> longs) {
        longs.forEach(id -> entityManager.remove(select(id)));
    }

    @Override
    public void delete(Publisher entity) {
        entityManager.remove(entity);
    }

    @Override
    public void delete(Iterable<Publisher> entities) {
        entities.forEach(entity -> entityManager.remove(entity));
    }
}
