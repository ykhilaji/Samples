package guice.hibernate.postgres.crud;

import guice.hibernate.postgres.crud.model.Entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class EntityManagerTest {
    @Test
    public void generalTest() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PostgreSQL");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Entity e = new Entity();
        em.persist(e);
        tx.commit();

        assertNotNull(e.getCreateTime());

        em.close();
    }
}
