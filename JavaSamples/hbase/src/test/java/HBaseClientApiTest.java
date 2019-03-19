import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HBaseClientApiTest {
    private static Configuration configuration;
    private static TableName tableName;

    @BeforeAll
    public static void beforeAll() throws IOException {
        configuration = HBaseConfiguration.create();
        try(Connection connection = ConnectionFactory.createConnection(configuration)) {
            tableName = TableName.valueOf("test_table");
            Admin admin = connection.getAdmin();
            if (!admin.tableExists(tableName)) {
                HTableDescriptor descriptor = new HTableDescriptor(tableName);
                descriptor.addFamily(new HColumnDescriptor("f1").setMaxVersions(3));
                descriptor.addFamily(new HColumnDescriptor("f2").setMaxVersions(3));

                admin.createTable(descriptor);
            }
        }
    }

    @AfterAll
    public static void afterAll() throws IOException {
        try(Connection connection = ConnectionFactory.createConnection(configuration)) {
            Admin admin = connection.getAdmin();
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        }
    }

    @Test
    public void getByNotExistingKey() throws IOException {
        Get get = new Get("getByNotExistingKey".getBytes());


        try(Connection connection = ConnectionFactory.createConnection(configuration)) {
            Table table = connection.getTable(tableName);
            Result result = table.get(get);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void putRow() throws IOException {
        Put put = new Put("put".getBytes());
        put.addColumn("f1".getBytes(), "c1".getBytes(), "val1".getBytes());
        put.addColumn("f2".getBytes(), "c2".getBytes(), "val2".getBytes());

        Get get = new Get("put".getBytes());

        try(Connection connection = ConnectionFactory.createConnection(configuration)) {
            Table table = connection.getTable(tableName);
            table.put(put);
            Result result = table.get(get);

            assertTrue(!result.isEmpty());
            assertArrayEquals(result.getValue("f1".getBytes(), "c1".getBytes()), "val1".getBytes());
            assertArrayEquals(result.getValue("f2".getBytes(), "c2".getBytes()), "val2".getBytes());
        }
    }

    @Test
    public void putRowAndGetOnlyOneColumnFamily() throws IOException {
        Put put = new Put("putRowAndGetOnlyOneColumnFamily".getBytes());
        put.addColumn("f1".getBytes(), "c1".getBytes(), "val1".getBytes());
        put.addColumn("f2".getBytes(), "c2".getBytes(), "val2".getBytes());

        Get get = new Get("putRowAndGetOnlyOneColumnFamily".getBytes());
        get.addFamily("f1".getBytes());

        try(Connection connection = ConnectionFactory.createConnection(configuration)) {
            Table table = connection.getTable(tableName);
            table.put(put);
            Result result = table.get(get);

            assertTrue(!result.isEmpty());
            assertArrayEquals(result.getValue("f1".getBytes(), "c1".getBytes()), "val1".getBytes());
            assertFalse(result.containsColumn("f2".getBytes(), "c2".getBytes()));
        }
    }

    @Test
    public void getMultipleVersionsOfSingleRow() throws IOException {
        Put put1 = new Put("getMultipleVersionsOfSingleRow".getBytes());
        put1.addColumn("f1".getBytes(), "c1".getBytes(), "val1".getBytes());
        put1.addColumn("f2".getBytes(), "c2".getBytes(), "val2".getBytes());

        Put put2 = new Put("getMultipleVersionsOfSingleRow".getBytes());
        put2.addColumn("f1".getBytes(), "c1".getBytes(), "val1Updated".getBytes());
        put2.addColumn("f2".getBytes(), "c2".getBytes(), "val2Updated".getBytes());

        Get get = new Get("getMultipleVersionsOfSingleRow".getBytes());
        get.setMaxVersions(2);

        try(Connection connection = ConnectionFactory.createConnection(configuration)) {
            Table table = connection.getTable(tableName);
            table.put(put1);
            table.put(put2);
            Result result = table.get(get);

            List<Cell> cells = result.getColumnCells("f1".getBytes(), "c1".getBytes());
            assertEquals(cells.size(), 2);
        }
    }

    @Test
    public void delete() throws IOException {
        Put put = new Put("delete".getBytes());
        put.addColumn("f1".getBytes(), "c1".getBytes(), "val1".getBytes());
        put.addColumn("f2".getBytes(), "c2".getBytes(), "val2".getBytes());

        Get get = new Get("delete".getBytes());
        Delete delete = new Delete("delete".getBytes());

        try(Connection connection = ConnectionFactory.createConnection(configuration)) {
            Table table = connection.getTable(tableName);
            table.put(put);

            assertTrue(table.exists(get));
            table.delete(delete);
            assertFalse(table.exists(get));
        }
    }
}
