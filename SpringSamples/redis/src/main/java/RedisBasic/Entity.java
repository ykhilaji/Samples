package RedisBasic;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("entity")
public class Entity {
    @Id
    private long id;
    @Indexed
    private String first;
    @Indexed
    private String second;
    private String value;

    public Entity() {
    }

    public Entity(long id, String first, String second, String value) {
        this.id = id;
        this.first = first;
        this.second = second;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @TimeToLive
    public long ttl() {
        return 10L;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", first='" + first + '\'' +
                ", second='" + second + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
