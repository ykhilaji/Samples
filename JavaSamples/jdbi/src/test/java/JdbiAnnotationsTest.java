import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

public class JdbiAnnotationsTest {
    static final Jdbi jdbi = Jdbi.create("jdbc:postgresql://192.168.99.100:5432/postgres", "postgres", "");

    @BeforeAll
    public static void init() {
        jdbi.installPlugin(new SqlObjectPlugin());

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
    public void findOneTest() {
        Optional<Entity> e = jdbi.withHandle(h -> {
            EntityRepository repository = h.attach(EntityRepository.class);
            return repository.findOne(1);
        });

        assertTrue(!e.isPresent());

        Optional<Entity> t = jdbi.withHandle(h -> {
            EntityRepository repository = h.attach(EntityRepository.class);
            Entity r = repository.insert("val");
            return repository.findOne(r.getId());
        });

        assertTrue(t.isPresent());
    }

    @Test
    public void findAllTest() {
        List<Entity> entityList = jdbi.withHandle(h -> {
            EntityRepository repository = h.attach(EntityRepository.class);
            repository.bulkInsert("val1", "val2", "val3");
            return repository.findAll();
        });

        assertTrue(entityList.size() == 3);
    }
}
