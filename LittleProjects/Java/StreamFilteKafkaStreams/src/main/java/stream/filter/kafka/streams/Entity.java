package stream.filter.kafka.streams;

import java.util.Objects;

public class Entity {
    private String key;

    public Entity() {
    }

    public Entity(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(key, entity.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "key='" + key + '\'' +
                '}';
    }

    public static Entity apply(String key) {
        return new Entity(key);
    }
}
