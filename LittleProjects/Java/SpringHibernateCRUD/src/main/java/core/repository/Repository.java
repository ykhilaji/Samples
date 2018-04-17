package core.repository;

import java.io.Serializable;

public interface Repository<T, ID extends Serializable> {
    T select(ID id);

    Iterable<T> select();

    void deleteById(ID id);

    void deleteById(Iterable<ID> ids);

    void delete(T entity);

    void delete(Iterable<T> entities);

    T insert(T entity);

    Iterable<T> insert(Iterable<T> entities);

    T update(T entity);

    Iterable<T> update(Iterable<T> entities);
}
