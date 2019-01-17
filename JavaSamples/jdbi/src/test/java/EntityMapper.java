import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityMapper implements RowMapper<Entity> {
    @Override
    public Entity map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Entity(rs.getInt(1), rs.getString(2));
    }
}
