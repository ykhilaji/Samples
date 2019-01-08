package guice.hibernate.postgres.crud.repository;

import java.util.Optional;

public interface Repository<A, Id> {
    Optional<A> findOne(Id id);

    Iterable<A> findAll();

    A save(A a);

    A update(A a);

    boolean deleteOne(Id id);

    long deleteAll();
}
