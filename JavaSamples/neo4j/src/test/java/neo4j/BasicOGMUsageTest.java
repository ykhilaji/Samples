package neo4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BasicOGMUsageTest {
    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void beforeAll() {
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://192.168.99.100:7687")
                .build();

        sessionFactory = new SessionFactory(configuration, "neo4j");
    }

    @AfterAll
    public static void afterAll() {
        sessionFactory.close();
    }

    @AfterEach
    public void truncate() {
        sessionFactory.openSession().purgeDatabase();
    }

    @Test
    public void crud() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        try {
            Entity entity = new Entity();
            Address address = new Address();
            LiveRelationship liveRelationship = new LiveRelationship();

            entity.setId(1);
            entity.setValue("some value");

            address.setCoutry("country");
            address.setCity("city");
            address.getEntities().add(entity);

            liveRelationship.setAddress(address);
            liveRelationship.setEntity(entity);
            liveRelationship.setSince(new Date());

            session.save(liveRelationship);

            assertEquals(session.load(Entity.class, 1L), entity);
            Address result = session.load(Address.class, address.getId());
            assertEquals(result, address);
            assertTrue(result.getEntities().contains(entity));

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }
    }
}

@RelationshipEntity(type = "live")
class LiveRelationship {
    @Id
    @GeneratedValue
    private long id;

    @DateLong
    private Date since;

    @StartNode
    private Address address;

    @EndNode
    private Entity entity;

    public LiveRelationship() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "neo4j.LiveRelationship{" +
                "since=" + since +
                '}';
    }
}

@NodeEntity(label = "entity")
class Entity {
    @Id
    @GeneratedValue
    private long id;
    private String value;

    public Entity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "neo4j.Entity{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id &&
                Objects.equals(value, entity.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}

@NodeEntity(label = "address")
class Address {
    @Id
    @GeneratedValue
    private long id;
    private String coutry;
    private String city;
    @Relationship(value = "live", direction = Relationship.INCOMING)
    private Set<Entity> entities = new HashSet<>();

    public Address() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCoutry() {
        return coutry;
    }

    public void setCoutry(String coutry) {
        this.coutry = coutry;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public void setEntities(Set<Entity> entities) {
        this.entities = entities;
    }

    @Override
    public String toString() {
        return "neo4j.Address{" +
                "coutry='" + coutry + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
