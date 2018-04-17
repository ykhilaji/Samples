package core.repository;

import core.model.Book;
import core.model.Book_;
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
public class BookRepositoryImpl implements BookRepository {
    private static Logger logger = Logger.getLogger(AuthorRepositoryImpl.class.getSimpleName());
    @PersistenceContext
    private EntityManager entityManager;

    public Book select(Long aLong) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Book> criteriaQuery = criteriaBuilder.createQuery(Book.class);
        Root<Book> root = criteriaQuery.from(Book.class);

        criteriaQuery
                .select(root)
                .where(criteriaBuilder.equal(root.get(Book_.id), aLong));

        List<Book> books = entityManager.createQuery(criteriaQuery).getResultList();

        if (books.isEmpty()) {
            return new Book();
        } else {
            return books.get(0);
        }
    }

    public Iterable<Book> select() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Book> criteriaQuery = criteriaBuilder.createQuery(Book.class);
        Root<Book> root = criteriaQuery.from(Book.class);

        criteriaQuery.select(root);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public Book insert(Book entity) {
        entityManager.persist(entity);

        return entity;
    }

    public Iterable<Book> insert(Iterable<Book> entities) {
        entities.forEach(entityManager::persist);

        return entities;
    }

    public Book update(Book entity) {
        Book book = entityManager.merge(entity);

        return book;
    }

    public Iterable<Book> update(Iterable<Book> entities) {
        List<Book> books = new ArrayList<>();

        entities.forEach(a -> books.add(entityManager.merge(a)));

        return books;
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
    public void delete(Book entity) {
        entityManager.remove(entity);
    }

    @Override
    public void delete(Iterable<Book> entities) {
        entities.forEach(entity -> entityManager.remove(entity));
    }
}
