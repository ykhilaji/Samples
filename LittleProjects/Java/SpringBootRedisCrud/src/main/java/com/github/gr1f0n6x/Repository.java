package com.github.gr1f0n6x;

import org.springframework.data.repository.CrudRepository;

@org.springframework.stereotype.Repository
public interface Repository extends CrudRepository<Entity, Long> {
}
