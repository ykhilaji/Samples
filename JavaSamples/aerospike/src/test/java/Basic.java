import com.aerospike.client.*;
import com.aerospike.client.policy.*;
import com.aerospike.client.query.*;
import com.aerospike.client.task.RegisterTask;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Basic {
    // thread-safe
    private static AerospikeClient client;

    private WritePolicy writePolicy;
    private Policy readPolicy;

    @BeforeAll
    public static void beforeAll() {
        ClientPolicy policy = new ClientPolicy();

        client = new AerospikeClient(policy, "localhost", 3000);
    }

    @AfterAll
    public static void afterAll() {
        client.truncate(null, "namespace_test", null, Calendar.getInstance());
        client.close();
    }

    @AfterEach
    public void after() {
        // truncate all sets in specified namespace using default policy
        client.truncate(null, "namespace_test", null, Calendar.getInstance());
    }

    @BeforeEach
    public void before() {
        writePolicy = new WritePolicy();
        readPolicy = new Policy();
    }

    @Test
    public void findByKey() {
        Key key = new Key("namespace_test", "set_name_test", "findByKey");
        Bin bin = new Bin("column", "value");
        // sync put
        client.put(writePolicy, key, bin);
        Record record = client.get(readPolicy, key);
        assertEquals(record.getString("column"), "value");
    }

    @Test
    public void ttl() throws InterruptedException {
        writePolicy.expiration = 1; // 1 second
        Key key = new Key("namespace_test", "set_name_test", "ttl");
        Bin bin = new Bin("column", "value");
        // sync put
        client.put(writePolicy, key, bin);
        readPolicy.consistencyLevel = ConsistencyLevel.CONSISTENCY_ONE;
        Record record = client.get(readPolicy, key);
        assertEquals(record.getString("column"), "value");
        Thread.sleep(2000);
        record = client.get(readPolicy, key);
        assertNull(record);
    }

    @Test
    public void delete() throws InterruptedException {
        Key key = new Key("namespace_test", "set_name_test", "delete");
        Bin bin = new Bin("column", "value");
        // sync put
        client.put(writePolicy, key, bin);
        Record record = client.get(readPolicy, key);
        assertEquals(record.getString("column"), "value");
        client.delete(writePolicy, key);
        record = client.get(readPolicy, key);
        assertNull(record);
    }

    @Test
    public void exists() {
        Key key = new Key("namespace_test", "set_name_test", "exists");
        Bin bin = new Bin("column", "value");
        assertFalse(client.exists(readPolicy, key));
        client.put(writePolicy, key, bin);
        assertTrue(client.exists(readPolicy, key));
    }

    @Test
    public void append() {
        Key key = new Key("namespace_test", "set_name_test", "append");
        Bin bin = new Bin("column", "value");
        client.put(writePolicy, key, bin);
        Record record = client.get(readPolicy, key);
        assertEquals(record.getString("column"), "value");
        // only for string values
        client.append(writePolicy, key, new Bin("column", "_part_two"));
        record = client.get(readPolicy, key);
        assertEquals(record.getString("column"), "value_part_two");
    }

    @Test
    public void add() {
        Key key = new Key("namespace_test", "set_name_test", "add");
        Bin bin = new Bin("column", 1);
        client.put(writePolicy, key, bin);
        Record record = client.get(readPolicy, key);
        assertEquals(record.getInt("column"), 1);
        // only for int values
        client.add(writePolicy, key, new Bin("column", 2));
        record = client.get(readPolicy, key);
        assertEquals(record.getInt("column"), 3);
    }

    @Test
    public void addNewBinToExistingRecord() {
        Key key = new Key("namespace_test", "set_name_test", "addNewBinToExistingRecord");
        Bin bin = new Bin("column", 1);
        client.put(writePolicy, key, bin);
        Record record = client.get(readPolicy, key);
        assertEquals(record.bins.size(), 1);
        writePolicy.recordExistsAction = RecordExistsAction.UPDATE; // default
        client.put(writePolicy, key, new Bin("column2", 2));
        record = client.get(readPolicy, key);
        assertEquals(record.bins.size(), 2);
    }

    @Test
    public void scan() {
        Key key = new Key("namespace_test", "set_name_test", "scan");
        Bin bin = new Bin("column", 1);
        client.put(writePolicy, key, bin);
        ScanPolicy policy = new ScanPolicy();
        client.scanAll(policy, "namespace_test", "set_name_test", (key1, record) -> {
            // some logic
        });
    }

    @Test
    public void query() {
        try {
            client.createIndex(readPolicy, "namespace_test", "query", "index", "column", IndexType.NUMERIC);

            Key key1 = new Key("namespace_test", "query", "1");
            Bin bin1 = new Bin("column", 1);
            Key key2 = new Key("namespace_test", "query", "2");
            Bin bin2 = new Bin("column", 2);
            Key key3 = new Key("namespace_test", "query", "3");
            Bin bin3 = new Bin("column", 3);

            client.put(writePolicy, key1, bin1);
            client.put(writePolicy, key2, bin2);
            client.put(writePolicy, key3, bin3);
            QueryPolicy queryPolicy = new QueryPolicy();
            Statement statement = new Statement();
            statement.setNamespace("namespace_test");
            statement.setSetName("query");
            statement.setFilter(Filter.range("column", 1, 2));
            RecordSet set = client.query(queryPolicy, statement);

            List<KeyRecord> array = new ArrayList<>();
            set.iterator().forEachRemaining(array::add);

            assertEquals(2, array.size());
        } finally {
            client.dropIndex(readPolicy, "namespace_test", "query", "index");
        }
    }

    @Test
    public void aggregate() throws InterruptedException {
        try {
            client.createIndex(readPolicy, "namespace_test", "query", "index", "column", IndexType.NUMERIC);
            Key key1 = new Key("namespace_test", "query", "1");
            Bin bin1 = new Bin("column", 1);
            Key key2 = new Key("namespace_test", "query", "2");
            Bin bin2 = new Bin("column", 2);
            client.put(writePolicy, key1, bin1);
            client.put(writePolicy, key2, bin2);

            // will create code.lua file in directory relative to configured script directory
            RegisterTask task = client.registerUdfString(readPolicy, "local function reducer(val1,val2)\n" +
                    "  return val1 + val2\n" +
                    "end\n" +
                    "\n" +
                    "function sum_single_bin(stream,name)\n" +
                    "  local function mapper(rec)\n" +
                    "    return rec[name]\n" +
                    "  end\n" +
                    "  return stream : map(mapper) : reduce(reducer)\n" +
                    "end\n", "code.lua", Language.LUA);

            task.waitTillComplete();
            QueryPolicy queryPolicy = new QueryPolicy();
            Statement statement = new Statement();
            statement.setNamespace("namespace_test");
            statement.setSetName("query");
            statement.setAggregateFunction("code", "sum_single_bin", Value.get("column"));
            ResultSet set = client.queryAggregate(queryPolicy, statement);
            set.next();
            long sum = (long) set.getObject();
            assertEquals(3, sum);
        } finally {
            client.dropIndex(readPolicy, "namespace_test", "query", "index");
        }
    }
}
