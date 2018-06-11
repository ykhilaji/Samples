import com.mchange.v2.c3p0.DataSources;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PooledAndUnpooledConnections {
    public static void main(String[] args) throws SQLException {
        DataSource dsPooled = null;
        try {
            DataSource dsUnpooled = DataSources.unpooledDataSource("jdbc:postgresql://192.168.99.100:5432/postgres",
                    "postgres",
                    "");
            dsPooled = DataSources.pooledDataSource(dsUnpooled);

            ResultSet resultSet = dsPooled.getConnection().createStatement().executeQuery("select table_schema, table_name from information_schema.tables");

            while (resultSet.next()) {
                System.out.println(String.format("%s.%s", resultSet.getString(1), resultSet.getString(2)));
            }
        } finally {
            DataSources.destroy(dsPooled);
        }
    }
}
