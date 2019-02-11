package crud.model;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class Entity {
    private long id;
    private String value;

    public Entity() {
    }

    public Entity(long id, String value) {
        this.id = id;
        this.value = value;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
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

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }

    public static class EntityMapper implements RowMapper<Entity> {
        @Override
        public Entity map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Entity(rs.getLong(1), rs.getString(2));
        }
    }
}
