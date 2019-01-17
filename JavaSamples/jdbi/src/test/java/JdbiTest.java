import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class JdbiTest {
    static final Jdbi jdbi = Jdbi.create("jdbc:postgresql://192.168.99.100:5432/postgres", "postgres", "");

    @BeforeAll
    public static void init() {
        jdbi.useHandle(h -> {
            h.execute("create table if not exists entity(id serial primary key, value varchar(255) not null)");
        });
    }

    @BeforeEach
    public void truncate() {
        jdbi.useHandle(h -> {
            h.execute("truncate table entity");
        });

        jdbi.registerRowMapper(Entity.class, null);
    }

    @Test
    public void simpleSelect() {
        int i = jdbi.withHandle(h -> h.select("select 1")
                .mapTo(Integer.class)
                .findOnly());

        assertEquals(1, i);
    }

    @Test
    public void insertAndSelectMappedPOJO() {
        Entity e = jdbi.withHandle(h -> {
            h.createUpdate("insert into entity(id, value) values (:id, :value)")
                    .bind("id", 1)
                    .bind("value", "some value")
                    .execute();

            return h.select("select id, value from entity")
                    .map((rs, ctx) -> new Entity(rs.getInt(1), rs.getString(2)))
                    .findOnly();
        });

        assertEquals(e, new Entity(1, "some value"));
    }

    @Test
    public void insertAndSelectMappedPOJOWithRowMapperImpl() {
        class EntityMapper implements RowMapper<Entity> {
            @Override
            public Entity map(ResultSet rs, StatementContext ctx) throws SQLException {
                return new Entity(rs.getInt(1), rs.getString(2));
            }
        }

        Entity e = jdbi.withHandle(h -> {
            h.createUpdate("insert into entity(id, value) values (:id, :value)")
                    .bind("id", 1)
                    .bind("value", "some value")
                    .execute();

            return h.select("select id, value from entity")
                    .map(new EntityMapper())
                    .findOnly();
        });

        assertEquals(e, new Entity(1, "some value"));
    }

    @Test
    public void insertAndSelectMappedPOJOWithRegisteredMapper() {
        jdbi.registerRowMapper(Entity.class, (rs, ctx) -> new Entity(rs.getInt(1), rs.getString(2)));

        Entity e = jdbi.withHandle(h -> {
            h.createUpdate("insert into entity(id, value) values (:id, :value)")
                    .bind("id", 1)
                    .bind("value", "some value")
                    .execute();

            return h.select("select id, value from entity")
                    .mapTo(Entity.class)
                    .findOnly();
        });

        assertEquals(e, new Entity(1, "some value"));
    }

    @Test
    public void batchInsert() {
        int r = jdbi.withHandle(h -> {
            PreparedBatch p = h.prepareBatch("insert into entity(id, value) values (:id, :value)");

            for (int i = 0; i < 10; ++i) {
                p.bind("id", i).bind("value", String.valueOf(i)).add();
            }

            return p.execute().length;
        });

        assertEquals(10, r);
    }

    @Test
    public void generatedIdReturn() {
        Entity e = jdbi.withHandle(h -> h.createUpdate("insert into entity(value) values(:value)")
        .bind("value", "val")
        .executeAndReturnGeneratedKeys()
        .map((rs, ctx) -> new Entity(rs.getInt(1), rs.getString(2)))
        .findOnly());

        assertTrue(e.getId() != 0);
    }

    @Test
    public void transactions() {
        jdbi.useHandle(h -> h.useTransaction(tx -> {
            tx.createUpdate("insert into entity(value) values(:value)")
                    .bind("value", "v")
                    .execute();

            tx.rollback();
        }));

        int r = jdbi.withHandle(h -> h.select("select count(*) from entity")
                .mapTo(Integer.class)
                .findOnly());

        assertEquals(0, r);
    }
}
