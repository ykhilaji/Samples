package com.github.gr1f0n6x.springbootcassandracrud.repository;

import com.github.gr1f0n6x.springbootcassandracrud.model.Entity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository // not necessary
public interface EntityRepository extends CassandraRepository<Entity, Entity.Key> {
}
