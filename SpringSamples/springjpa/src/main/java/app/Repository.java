package app;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@org.springframework.stereotype.Repository
@Transactional
public class Repository {
    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(Entity entity) {
        System.out.println(entity);
        entityManager.persist(entity);
        System.out.println(entity);
    }

    public void delete(Entity entity) {
        Entity f = entityManager.find(Entity.class, entity.getId());
        entityManager.remove(f);
    }

    public Entity find(long id) {
        return entityManager.find(Entity.class, id);
    }

    public void update(Entity entity) {
        Entity f = entityManager.find(Entity.class, entity.getId());
        f.setValue(entity.getValue());

        entityManager.merge(f);
    }

    public void selectAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Entity> select = builder.createQuery(Entity.class);
        Root<Entity> root = select.from(Entity.class);

        entityManager.createQuery(select).getResultStream().forEach(System.out::println);
    }
}
