package guice.redis.crud.repository;

import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findOne(ID id) throws Exception;

    T save(T t) throws Exception;

    T update(T t) throws Exception;

    void deleteOne(ID id) throws Exception;
}
