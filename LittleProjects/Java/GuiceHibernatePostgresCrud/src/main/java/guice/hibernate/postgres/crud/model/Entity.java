package guice.hibernate.postgres.crud.model;

import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;


@javax.persistence.Entity
@Table(name = "entity", schema = "guice_crud")
@org.hibernate.annotations.NamedQueries({
        @org.hibernate.annotations.NamedQuery(
                name = "ALL_ENTITIES",
                query = "select e from Entity e",
                comment = "select all entities"
        ),
        @org.hibernate.annotations.NamedQuery(
                name = "DELETE_ENTITY_BY_ID",
                query = "delete from Entity e where e.id = :id",
                comment = "delete a single row by id"
        ),
        @org.hibernate.annotations.NamedQuery(
                name = "DELETE_ALL_ENTITIES",
                query = "delete from Entity e",
                comment = "delete all rows"
        )
})
public class Entity {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "ID_GENERATOR")
    @org.hibernate.annotations.GenericGenerator(
            name = "ID_GENERATOR",
            strategy = "enhanced-sequence",
            parameters = {
                    @org.hibernate.annotations.Parameter(
                            name = "schema",
                            value = "guice_crud"
                    ),
                    @org.hibernate.annotations.Parameter(
                            name = "sequence_name",
                            value = "entity_id_seq"
                    ),
                    @org.hibernate.annotations.Parameter(
                            name = "initial_value",
                            value = "1"
                    ),
            }
    )
    private long id;
    @Column(name = "value")
    private String value;
    @Column(name = "create_time", insertable = false, updatable = false)
    @org.hibernate.annotations.Generated(value = GenerationTime.ALWAYS)
    private LocalDateTime createTime;

    public Entity() {
    }

    public Entity(long id, String value, LocalDateTime createTime) {
        this.id = id;
        this.value = value;
        this.createTime = createTime;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id &&
                Objects.equals(value, entity.value) &&
                Objects.equals(createTime, entity.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, createTime);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
