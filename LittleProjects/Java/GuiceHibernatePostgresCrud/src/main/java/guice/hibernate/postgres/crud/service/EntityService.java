package guice.hibernate.postgres.crud.service;

import guice.hibernate.postgres.crud.model.Entity;
import guice.hibernate.postgres.crud.repository.EntityRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Collections;
import java.util.Optional;

public class EntityService implements Service<Entity, Long> {
    private Logger logger = LogManager.getLogger("entity-service");
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("PostgreSQL");

    @Override
    public Optional<Entity> findOne(Long aLong) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Optional<Entity> e = new EntityRepository(em).findOne(aLong);
            tx.commit();
            return e;
        } catch (Exception e) {
            tx.rollback();
            logger.error(e.getLocalizedMessage());
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public Iterable<Entity> findAll() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Iterable<Entity> e = new EntityRepository(em).findAll();
            tx.commit();
            return e;
        } catch (Exception e) {
            tx.rollback();
            logger.error(e.getLocalizedMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    @Override
    public Entity save(Entity entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            new EntityRepository(em).save(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            tx.rollback();
            logger.error(e.getLocalizedMessage());
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Entity update(Entity entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            new EntityRepository(em).update(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            tx.rollback();
            logger.error(e.getLocalizedMessage());
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean deleteOne(Long aLong) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            boolean r = new EntityRepository(em).deleteOne(aLong);
            tx.commit();
            return r;
        } catch (Exception e) {
            tx.rollback();
            logger.error(e.getLocalizedMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public long deleteAll() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            long r = new EntityRepository(em).deleteAll();
            tx.commit();
            return r;
        } catch (Exception e) {
            tx.rollback();
            logger.error(e.getLocalizedMessage());
            return 0;
        } finally {
            em.close();
        }
    }
}
