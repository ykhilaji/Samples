package com.github.gr1f0n6x.springbootignitecrud;

import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;

@RepositoryConfig(cacheName = "entity")
public interface EntityRepository extends IgniteRepository<Entity, Long> {
}
