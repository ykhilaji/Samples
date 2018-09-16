package app;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.persistence.criteria.*;

public class App {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Configuration.class);
        SessionFactory sessionFactory = context.getBean(SessionFactory.class);

        Entity entity = new Entity();
        entity.setValue("value");
        System.out.println(entity);


        try(Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(entity);
            System.out.println(entity);
            entity = session.find(Entity.class, 1L);
            System.out.println(entity);
            session.delete(entity);
            entity = session.find(Entity.class, 1L);
            System.out.println(entity);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try(Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            Entity entity1 = new Entity();
            entity1.setValue("1");
            Entity entity2 = new Entity();
            entity2.setValue("2");

            session.save(entity1);
            session.save(entity2);

            CriteriaBuilder builder = sessionFactory.createEntityManager().getCriteriaBuilder();

            System.out.println("USING CRITERIA");
            System.out.println("SELECT ALL");
            CriteriaQuery<Entity> selectAll = builder.createQuery(Entity.class);
            selectAll.select(selectAll.from(Entity.class));
            session.createQuery(selectAll).getResultStream().forEach(System.out::println);

            System.out.println("SELECT WHERE VALUE EQUAL 2");
            CriteriaQuery<Entity>  selectWhere = builder.createQuery(Entity.class);
            Root<Entity> root = selectWhere.from(Entity.class);
            selectWhere.select(root).where(builder.equal(root.get("value"), "2"));
            session.createQuery(selectWhere).getResultStream().forEach(System.out::println);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try(Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            CriteriaBuilder builder = sessionFactory.createEntityManager().getCriteriaBuilder();
            CriteriaQuery<Entity> selectAll = builder.createQuery(Entity.class);
            selectAll.select(selectAll.from(Entity.class));

            System.out.println("UPDATE VALUE TO 'UPDATED' WHERE VALUE EQUAL 2");
            CriteriaUpdate<Entity> updateWhere = builder.createCriteriaUpdate(Entity.class);
            Root<Entity> root = updateWhere.from(Entity.class);
            updateWhere.set("value", "updated").where(builder.equal(root.get("value"), "2"));
            session.createQuery(updateWhere).executeUpdate();
            session.createQuery(selectAll).getResultStream().forEach(System.out::println);

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try(Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            CriteriaBuilder builder = sessionFactory.createEntityManager().getCriteriaBuilder();
            CriteriaQuery<Entity> selectAll = builder.createQuery(Entity.class);
            selectAll.select(selectAll.from(Entity.class));

            System.out.println("DELETE ALL");
            CriteriaDelete<Entity> deleteAll = builder.createCriteriaDelete(Entity.class);
            deleteAll.from(Entity.class);
            session.createQuery(deleteAll).executeUpdate();
            session.createQuery(selectAll).getResultStream().forEach(System.out::println);

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
