import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CassandraSample {
    private static Logger logger = LoggerFactory.getLogger("cassandra");

    public static void main(String[] args) {
        Cluster.Builder builder = Cluster.builder();
        builder.addContactPoint("192.168.99.100");
        builder.withPort(9042);

        try(Cluster cluster = builder.build();
            Session session = cluster.connect()) {
            RegularStatement statement = new SimpleStatement("create keyspace if not exists sample " +
                    "with replication  = {'class':'SimpleStrategy', 'replication_factor':1};");
            statement.setConsistencyLevel(ConsistencyLevel.QUORUM);
            ResultSet resultSet = session.execute(statement);

            logger.info(String.format("Was applied: %s", resultSet.wasApplied()));

            statement = new SimpleStatement("select * from system_schema.keyspaces;");
            resultSet = session.execute(statement);

            resultSet.forEach(row -> logger.info(String.format("Keyspace: %s", row.getString(0))));

            statement = new SimpleStatement("create table if not exists sample.test (" +
                    "id uuid primary key," +
                    "value text" +
                    ");");

            resultSet = session.execute(statement);
            logger.info(String.format("Was applied: %s", resultSet.wasApplied()));

            ExecutorService service = Executors.newSingleThreadExecutor();
            statement = new SimpleStatement("alter table sample.test add another_column text;");
            session.executeAsync(statement).addListener(() -> {
                logger.info("Column was added");
                session
                        .executeAsync("select column_name from system_schema.columns where keyspace_name=? and table_name=?;", "sample", "test")
                        .getUninterruptibly()
                        .forEach(row -> logger.info(row.getString(0)));

                session
                        .executeAsync("alter table sample.test drop another_column;")
                        .addListener(() -> {
                            logger.info("Column was dropped");
                            session
                                    .executeAsync("select column_name from system_schema.columns where keyspace_name=? and table_name=?;", "sample", "test")
                                    .getUninterruptibly()
                                    .forEach(row -> logger.info(row.getString(0)));
                        }, service);
            }, service);
            Thread.sleep(1000);

            resultSet = session.execute("insert into sample.test(id, value) values (uuid(), ?);", "some_value");
            resultSet.forEach(row -> logger.info(String.format("id: %s, value: %s", row.getObject(0), row.getString(1))));

            resultSet = session.execute("select * from sample.test");
            resultSet.forEach(row -> logger.info(String.format("id: %s, value: %s", row.getObject(0), row.getString(1))));

            resultSet = session.execute("begin batch " +
                    "insert into sample.test(id, value) values (uuid(), ?); " +
                    "insert into sample.test(id, value) values (uuid(), ?); " +
                    "apply batch;", "value1", "value2");
            resultSet.forEach(row -> logger.info(String.format("id: %s, value: %s", row.getObject(0), row.getString(1))));

            resultSet = session.execute("select * from sample.test");
            resultSet.forEach(row -> logger.info(String.format("id: %s, value: %s", row.getObject(0), row.getString(1))));

            resultSet = session.execute("drop table if exists sample.test;");
            resultSet.forEach(row -> logger.info(String.format("Result: %s", row.getString(0))));

            service.shutdown();
            logger.info("Finish");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
