import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class CassandraQueryBuilder {
    public static void main(String[] args) {
        Cluster cluster = Cluster
                .builder()
                .addContactPoint("192.168.99.100")
                .withPort(9042)
                .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM))
                .build();



        try (Session session = cluster.connect()) {
            session.execute("create keyspace if not exists sample with" +
                    " replication = {'class': 'SimpleStrategy', 'replication_factor':1} " +
                    " and durable_writes = true;");
            session.execute("create table if not exists sample.test (id int, value text, primary key (id));");
            session.execute("create index if not exists on sample.test (value);");

            session.execute(QueryBuilder
                    .insertInto("sample", "test")
                    .value("id", 1)
                    .value("value", "some inserted value")
                    .using(QueryBuilder.ttl(2)));

            session.execute(QueryBuilder
            .select()
            .from("sample", "test")
            .where(QueryBuilder.eq("id", 1)))
                    .forEach(row -> System.out.println(String.format("ID: %s, value: %s", row.getInt(0), row.getString(1))));

            session.execute(QueryBuilder
            .truncate("sample", "test"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cluster.close();
        }
    }
}
