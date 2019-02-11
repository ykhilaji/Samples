package crud.repository;

import java.util.Optional;

public interface Repository<E, ID> {
    E save(E e);

    boolean deleteById(ID id);

    E update(E e);

    Optional<E> findOne(ID id);

    Iterable<E> findAll();
}
