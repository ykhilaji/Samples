package hibernateannotation;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "entity", schema = "jdbc")
public class Entity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private int id;
    @Column(name = "value", length = 255)
    private String value;

    public Entity() {
    }

    public Entity(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (id != entity.id) return false;
        return value != null ? value.equals(entity.value) : entity.value == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
