import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;

import java.nio.ByteBuffer;
import java.util.UUID;

public class CassandraWithCustomSettings {
    public static void main(String[] args) {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setConsistencyLevel(ConsistencyLevel.QUORUM);
        queryOptions.setFetchSize(100);

        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setHeartbeatIntervalSeconds(30);
        poolingOptions.setMaxQueueSize(10);
        poolingOptions.setIdleTimeoutSeconds(60);
        poolingOptions.setConnectionsPerHost(HostDistance.LOCAL, 1, 2);

        Cluster cluster = Cluster
                .builder()
                .addContactPoint("192.168.99.100")
                .withPort(9042)
                .withCompression(ProtocolOptions.Compression.NONE)
                .withQueryOptions(queryOptions)
                .withPoolingOptions(poolingOptions)
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .build();

        cluster
                .getConfiguration()
                .getCodecRegistry()
                .register(new TypeCodec<String>(DataType.uuid(), String.class) {
                    @Override
                    public ByteBuffer serialize(String value, ProtocolVersion protocolVersion) throws InvalidTypeException {
                        return ByteBuffer.wrap(value.getBytes());
                    }

                    @Override
                    public String deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
                        return UUID.nameUUIDFromBytes(bytes.array()).toString();
                    }

                    @Override
                    public String parse(String value) throws InvalidTypeException {
                        return value;
                    }

                    @Override
                    public String format(String value) throws InvalidTypeException {
                        return value;
                    }
                });

        try (Session session = cluster.connect()) {
            session.execute("select * from system_schema.keyspaces;")
                    .forEach(row -> System.out.println(row.getString(0)));

            System.out.println("\nTABLES IN SYSTEM KEYSPACE\n");
            session.execute("select * from system_schema.tables where keyspace_name='system';")
                    .forEach(row -> System.out.println(row.getString(1)));

            session.execute("select uuid() from system.local;")
                    .forEach(row -> System.out.println(row.getString(0)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cluster.close();
        }
    }
}
