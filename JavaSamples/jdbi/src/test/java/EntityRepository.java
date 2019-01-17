import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface EntityRepository {
    @SqlQuery("select id, value from entity where id = :id")
    @RegisterRowMapper(EntityMapper.class)
    Optional<Entity> findOne(@Bind("id") int id);

    @SqlUpdate("insert into entity(value) values(:value)")
    @GetGeneratedKeys
    @RegisterRowMapper(EntityMapper.class)
    Entity insert(@Bind("value") String value);

    @SqlQuery("select id, value from entity")
    @RegisterRowMapper(EntityMapper.class)
    List<Entity> findAll();

    @SqlBatch("insert into entity(value) values (?)")
    void bulkInsert(String... values);
}
