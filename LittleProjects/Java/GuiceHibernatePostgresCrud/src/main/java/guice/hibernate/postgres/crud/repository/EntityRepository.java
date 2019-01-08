package guice.hibernate.postgres.crud.repository;

import guice.hibernate.postgres.crud.model.Entity;

import javax.persistence.EntityManager;
import java.util.Optional;

public class EntityRepository implements Repository<Entity, Long> {
    public EntityManager em;

    public EntityRepository() {
    }

    public EntityRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<Entity> findOne(Long aLong) {
        return Optional.of(em.find(Entity.class, aLong));
    }

    @Override
    public Iterable<Entity> findAll() {
        return em.createNamedQuery("ALL_ENTITIES", Entity.class).getResultList();
    }

    @Override
    public Entity save(Entity entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    public Entity update(Entity entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    public boolean deleteOne(Long aLong) {
        return em.createNamedQuery("DELETE_ENTITY_BY_ID").setParameter("id", aLong).executeUpdate() != 0;
    }

    @Override
    public long deleteAll() {
        return em.createNamedQuery("DELETE_ALL_ENTITIES").executeUpdate();
    }
}
