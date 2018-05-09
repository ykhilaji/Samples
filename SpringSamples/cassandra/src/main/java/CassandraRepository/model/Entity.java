package CassandraRepository.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "entity")
public class Entity {
    @PrimaryKey
    private Key id;
    @Column(value = "field")
    private String field;

    public Entity() {
    }

    public Entity(Key id, String field) {
        this.id = id;
        this.field = field;
    }

    public Key getId() {
        return id;
    }

    public void setId(Key id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", field='" + field + '\'' +
                '}';
    }
}
