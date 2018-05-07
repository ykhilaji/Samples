import com.datastax.driver.core.*;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.SchemaStatement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CassandraSchemaStatementSample {
    public static void main(String[] args) {
        Cluster cluster = Cluster
                .builder()
                .addContactPoint("192.168.99.100")
                .withPort(9042)
                .withInitialListeners(Collections.singletonList(new StateListener()))
                .build();

        cluster.register(new SchemaChangeListener());

        try (Session session = cluster.connect()) {
            Map<String, Object> map = new HashMap<>();
            map.put("class", "SimpleStrategy");
            map.put("replication_factor", 1);

            SchemaStatement keyspace = SchemaBuilder
                    .createKeyspace("sample")
                    .ifNotExists()
                    .with()
                    .replication(map)
                    .durableWrites(true);

            session.execute(keyspace);

            SchemaStatement table = SchemaBuilder
                    .createTable("sample", "test")
                    .ifNotExists()
                    .addPartitionKey("id", DataType.cint())
                    .addClusteringColumn("key", DataType.text())
                    .addColumn("value", DataType.text())
                    .withOptions()
                        .comment("Table inside sample keyspace")
                        .clusteringOrder("key", SchemaBuilder.Direction.ASC);

            session.execute(table);

            session.execute(SchemaBuilder.dropTable("sample", "test"));
            session.execute(SchemaBuilder.dropKeyspace("sample"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cluster.close();
        }
    }

    public static class StateListener implements Host.StateListener {
        @Override
        public void onAdd(Host host) {
            System.out.println("Add host");
        }

        @Override
        public void onUp(Host host) {
            System.out.println("Up host");
        }

        @Override
        public void onDown(Host host) {
            System.out.println("Down host");
        }

        @Override
        public void onRemove(Host host) {
            System.out.println("Remove host");
        }

        @Override
        public void onRegister(Cluster cluster) {
            System.out.println("Register cluster");
        }

        @Override
        public void onUnregister(Cluster cluster) {
            System.out.println("Unregister cluster");
        }
    }

    public static class SchemaChangeListener implements com.datastax.driver.core.SchemaChangeListener {
        @Override
        public void onKeyspaceAdded(KeyspaceMetadata keyspace) {
            System.out.println(String.format("Create keyspace: %s", keyspace.getName()));
        }

        @Override
        public void onKeyspaceRemoved(KeyspaceMetadata keyspace) {
            System.out.println("Drop keyspace");
        }

        @Override
        public void onKeyspaceChanged(KeyspaceMetadata current, KeyspaceMetadata previous) {
            System.out.println("Alter keyspace");
        }

        @Override
        public void onTableAdded(TableMetadata table) {
            System.out.println(String.format("Table created: %s in keyspace: %s", table.getName(), table.getKeyspace().getName()));
        }

        @Override
        public void onTableRemoved(TableMetadata table) {
            System.out.println("Drop table");
        }

        @Override
        public void onTableChanged(TableMetadata current, TableMetadata previous) {
            System.out.println("Alter table");
        }

        @Override
        public void onUserTypeAdded(UserType type) {

        }

        @Override
        public void onUserTypeRemoved(UserType type) {

        }

        @Override
        public void onUserTypeChanged(UserType current, UserType previous) {

        }

        @Override
        public void onFunctionAdded(FunctionMetadata function) {

        }

        @Override
        public void onFunctionRemoved(FunctionMetadata function) {

        }

        @Override
        public void onFunctionChanged(FunctionMetadata current, FunctionMetadata previous) {

        }

        @Override
        public void onAggregateAdded(AggregateMetadata aggregate) {

        }

        @Override
        public void onAggregateRemoved(AggregateMetadata aggregate) {

        }

        @Override
        public void onAggregateChanged(AggregateMetadata current, AggregateMetadata previous) {

        }

        @Override
        public void onMaterializedViewAdded(MaterializedViewMetadata view) {

        }

        @Override
        public void onMaterializedViewRemoved(MaterializedViewMetadata view) {

        }

        @Override
        public void onMaterializedViewChanged(MaterializedViewMetadata current, MaterializedViewMetadata previous) {

        }

        @Override
        public void onRegister(Cluster cluster) {

        }

        @Override
        public void onUnregister(Cluster cluster) {

        }
    }
}
