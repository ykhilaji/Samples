package com.github.gr1f0n6x.springbootcassandracrud.service;

import java.util.List;

public interface Service<E, PK> {
    E findById(PK pk);
    List<E> findAll();
    void deleteById(PK pk);
    void deleteAll();
    E save(E e);
    E update(E e);
}
