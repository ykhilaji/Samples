import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPool {
    public static void main(String[] args) throws PropertyVetoException, SQLException {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass( "org.postgresql.Driver" );
        cpds.setJdbcUrl( "jdbc:postgresql://192.168.99.100:5432/postgres" );
        cpds.setUser("postgres");
        cpds.setPassword("");

        cpds.setInitialPoolSize(5);
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);

        ResultSet resultSet = cpds.getConnection().createStatement().executeQuery("select table_schema, table_name from information_schema.tables");

        while (resultSet.next()) {
            System.out.println(String.format("%s.%s", resultSet.getString(1), resultSet.getString(2)));
        }
    }
}
