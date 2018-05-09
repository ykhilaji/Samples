package CassandraRepository.repository;

import CassandraRepository.model.Entity;
import CassandraRepository.model.Key;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CassandraRepositoryImpl extends CassandraRepository<Entity, Key> {}
