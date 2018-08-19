package com.github.gr1f0n6x;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface Repository extends CrudRepository<Entity, Long> {
    Iterable<Entity> findAllByValueIsLike(String value);
    Iterable<Entity> findAllByIdBetween(long min, long max);
    Iterable<Entity> findTop10ByOrderByIdAsc();
    @Query("select e from Entity e where e.value = ?1 ")
    Iterable<Entity> findByValue(String value);
}
