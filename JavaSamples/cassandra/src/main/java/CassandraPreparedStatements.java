import com.datastax.driver.core.*;

public class CassandraPreparedStatements {
    public static void main(String[] args) {
        Cluster cluster = Cluster
                .builder()
                .addContactPoint("192.168.99.100")
                .withPort(9042)
                .build();

        try(Session session = cluster.connect()) {
            session.execute("create keyspace if not exists sample with" +
                    " replication = {'class': 'SimpleStrategy', 'replication_factor':1} " +
                    " and durable_writes = true;");
            session.execute("create table if not exists sample.test (id int, value text, primary key (id));");
            session.execute("create index if not exists on sample.test (value);");

            PreparedStatement select = session.prepare("select * from sample.test where id = ?");
            select.setConsistencyLevel(ConsistencyLevel.QUORUM);

            PreparedStatement insert = session.prepare("insert into sample.test (id, value) values (?, ?) using ttl ?");
            insert.setConsistencyLevel(ConsistencyLevel.QUORUM);

            session.execute(insert.bind(1, "test", 2));
            ResultSet resultSet = session.execute(select.bind(1));
            resultSet.forEach(row -> System.out.println(String.format("ID: %s, value: %s", row.getInt(0), row.getString(1))));

            Thread.sleep(2500);

            System.out.println("Execute after 2.5 seconds");
            resultSet = session.execute(select.bind(1));
            resultSet.forEach(row -> System.out.println(String.format("ID: %s, value: %s", row.getInt(0), row.getString(1))));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cluster.close();
        }
    }
}
