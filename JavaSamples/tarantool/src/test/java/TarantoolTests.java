import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TarantoolTests {
    private static TarantoolClientImpl client;
    private static int spaceId;

    @BeforeAll
    public static void init() {
        TarantoolClientConfig config = new TarantoolClientConfig();
        client = new TarantoolClientImpl((i, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }

            try {
                return SocketChannel.open(new InetSocketAddress("192.168.99.100", 3301));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, config);

        client.syncOps().eval(String.format("box.schema.space.create('%s', {if_not_exists=true, temporary=true})", "test"));
        client.syncOps().eval(String.format("box.space.test:create_index('%s', {if_not_exists=true, type='HASH', unique=true, parts={1, 'scalar'}})", "primary"));
        client.syncOps().eval(String.format("box.space.test:create_index('%s', {if_not_exists=true, type='TREE', unique=false, parts={{2, 'scalar'}, {3, 'scalar', is_nullable=true}}})", "secondary"));

        spaceId = (int) ((List<Object>)client.syncOps().eval("return box.space.test.id")).get(0);
    }

    @AfterAll
    public static void close() {
        client.close();
    }

    @BeforeEach
    public void truncateSpace() {
        client.eval("box.space.test:truncate()");
    }

    @Test
    public void insert() {
        List<?> list = Arrays.asList("1", "2", "3");
        int indexId = 0;
        int offset = 0;
        int limit = 1;
        int iteratorType = 0; // EQ

        List<?> select = Arrays.asList("1");
        client.syncOps().insert(spaceId, list);
        List<?> result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);

        // return : [[1,2,3]]
        assertIterableEquals((Iterable<?>) result.get(0), list);
    }

    @Test
    public void delete() {
        List<?> list = Arrays.asList("1", "2", "3");
        int indexId = 0;
        int offset = 0;
        int limit = 1;
        int iteratorType = 0; // EQ

        List<?> select = Arrays.asList("1");
        client.syncOps().insert(spaceId, list);
        List<?> result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), list);
        client.syncOps().delete(spaceId, select);
        result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertTrue(result.isEmpty());
    }

    @Test
    public void update() {
        List<?> list = Arrays.asList(1, 2, 3);
        int indexId = 0;
        int offset = 0;
        int limit = 1;
        int iteratorType = 0; // EQ

        List<?> select = Arrays.asList(1);
        client.syncOps().insert(spaceId, list);
        List<?> result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), list);
        client.syncOps().update(spaceId, select, Arrays.asList("=", 1, "updated"), Arrays.asList("+", 2, 1), Arrays.asList("!", 3, "newField"));
        result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        List<?> updated = Arrays.asList(1, "updated", 4, "newField");
        assertIterableEquals((Iterable<?>) result.get(0), updated);
    }

    @Test
    public void upsert() {
        List<?> list = Arrays.asList(1, 2, 3);
        int indexId = 0;
        int offset = 0;
        int limit = 1;
        int iteratorType = 0; // EQ

        List<?> select = Arrays.asList(1);
        client.syncOps().insert(spaceId, list);
        List<?> result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), list);
        List<?> upsert = Arrays.asList(1, 3, 2);
        client.syncOps().upsert(spaceId, select, upsert, Arrays.asList("=", 1, 3), Arrays.asList("=", 2, 2));
        result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), upsert);
    }

    @Test
    public void selectUsingPrimaryIndex() {
        List<?> tuple1 = Arrays.asList(1, 2, 3);
        List<?> tuple2 = Arrays.asList(2, 2, 3);
        int indexId = 0;
        int offset = 0;
        int limit = 1;
        int iteratorType = 0; // EQ

        List<?> select = Arrays.asList(1);
        client.syncOps().insert(spaceId, tuple1);
        client.syncOps().insert(spaceId, tuple2);
        List<?> result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), tuple1);
        select = Arrays.asList(2);
        result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), tuple2);
    }

    @Test
    public void selectUsingSecondaryIndex() {
        List<?> tuple1 = Arrays.asList(1, 2, 3);
        List<?> tuple2 = Arrays.asList(2, 3, 4);
        List<?> tuple3 = Arrays.asList(3, 4, 5);
        int indexId = 1;
        int offset = 0;
        int limit = 1;
        int iteratorType = 6;

        List<?> select = Arrays.asList(3, 4);
        client.syncOps().insert(spaceId, tuple1);
        client.syncOps().insert(spaceId, tuple2);
        client.syncOps().insert(spaceId, tuple3);
        List<?> result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), tuple3);
        iteratorType = 3;
        result = client.syncOps().select(spaceId, indexId, select, offset, limit, iteratorType);
        assertIterableEquals((Iterable<?>) result.get(0), tuple1);
    }
}
