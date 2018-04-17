package core.repository;

import core.model.Author;
import core.model.Author_;
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
public class AuthorRepositoryImpl implements AuthorRepository {
    private static Logger logger = Logger.getLogger(AuthorRepositoryImpl.class.getSimpleName());
    @PersistenceContext
    private EntityManager entityManager;


    public Author select(Long aLong) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Author> criteriaQuery = criteriaBuilder.createQuery(Author.class);
        Root<Author> root = criteriaQuery.from(Author.class);

        criteriaQuery
                .select(root)
                .where(criteriaBuilder.equal(root.get(Author_.id), aLong));

        List<Author> authors = entityManager.createQuery(criteriaQuery).getResultList();

        if (authors.isEmpty()) {
            return new Author();
        } else {
            return authors.get(0);
        }
    }

    public Iterable<Author> select() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Author> criteriaQuery = criteriaBuilder.createQuery(Author.class);
        Root<Author> root = criteriaQuery.from(Author.class);

        criteriaQuery.select(root);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public Author insert(Author entity) {
        entityManager.persist(entity);

        return entity;
    }

    public Iterable<Author> insert(Iterable<Author> entities) {
        entities.forEach(entityManager::persist);

        return entities;
    }

    public Author update(Author entity) {
        Author author = entityManager.merge(entity);

        return author;
    }

    public Iterable<Author> update(Iterable<Author> entities) {
        List<Author> authors = new ArrayList<>();

        entities.forEach(a -> authors.add(entityManager.merge(a)));

        return authors;
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
    public void delete(Author entity) {
        entityManager.remove(entity);
    }

    @Override
    public void delete(Iterable<Author> entities) {
        entities.forEach(entity -> entityManager.remove(entity));
    }
}
