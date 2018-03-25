import org.junit.*;

import java.sql.*;

import static org.junit.Assert.*;

public class JdbcPostgresSample {
    private static final String URL = "jdbc:postgresql://192.168.99.100:5432/postgres";
    private static final String USER = "postgres";
    private static final String DRIVER = "org.postgresql.Driver";

    private static final String INSERT_INTO_ONE = "INSERT INTO one(id, value) VALUES (1, '1'), (2, '2'), (3, '3'), (4, '4'), (5, '5')";
    private static final String SELECT_COUNT_FROM_ONE = "SELECT COUNT(1) FROM one";
    private static final String SELECT_WHERE_ID_EQUAL = "SELECT id, value FROM one WHERE id=?";

    private static Connection connection;

    @BeforeClass
    public static void init() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER);
        connection = DriverManager.getConnection(URL, USER, "");

        Statement init = connection.createStatement();

        init.execute("CREATE TABLE IF NOT EXISTS one (" +
                "id bigint PRIMARY KEY," +
                "value varchar(255)," +
                "time TIMESTAMP DEFAULT now())");

        init.execute("CREATE TABLE IF NOT EXISTS two (" +
                "id bigint PRIMARY KEY," +
                "value varchar(255)," +
                "time TIMESTAMP DEFAULT now())");

        init.close();
    }

    @AfterClass
    public static void destroy() throws SQLException {
        Statement drop = connection.createStatement();

        drop.execute("DROP TABLE one CASCADE ");
        drop.execute("DROP TABLE two CASCADE ");

        drop.close();

        connection.close();
    }

    @Before
    public void truncate() throws SQLException {
        Statement truncate = connection.createStatement();

        truncate.execute("TRUNCATE TABLE one");
        truncate.execute("TRUNCATE TABLE two");

        truncate.close();
    }

    @After
    public void after() throws SQLException {
        connection.setAutoCommit(true);
    }

    @Test
    public void insert() throws SQLException {
        Statement insert = connection.createStatement();
        insert.execute(INSERT_INTO_ONE);

        int rows = insert.getUpdateCount();

        insert.close();

        assertEquals(5, rows);

        PreparedStatement select = connection.prepareStatement(SELECT_WHERE_ID_EQUAL);
        select.setLong(1, 1L);
        select.execute();
        ResultSet resultSet = select.getResultSet();

        resultSet.next();

        assertEquals(1, resultSet.getLong(1));
        assertEquals("1", resultSet.getString(2));

        select.close();
    }

    @Test
    public void autoCommitFalseCommit() throws SQLException {
        Statement statement = connection.createStatement();

        connection.setAutoCommit(false);

        statement.execute(INSERT_INTO_ONE);
        assertEquals(5, statement.getUpdateCount());

        ResultSet resultSet = statement.executeQuery(SELECT_COUNT_FROM_ONE);
        resultSet.next();

        assertEquals(5, resultSet.getLong(1));

        statement.close();

        connection.commit();
        connection.setAutoCommit(true);

        statement = connection.createStatement();

        resultSet = statement.executeQuery(SELECT_COUNT_FROM_ONE);
        resultSet.next();

        assertEquals(5, resultSet.getLong(1));

        statement.close();
    }

    @Test
    public void autoCommitFalseRollback() throws SQLException {
        Statement statement = connection.createStatement();

        connection.setAutoCommit(false);

        statement.execute(INSERT_INTO_ONE);
        assertEquals(5, statement.getUpdateCount());

        ResultSet resultSet = statement.executeQuery(SELECT_COUNT_FROM_ONE);
        resultSet.next();

        assertEquals(5, resultSet.getLong(1));

        statement.close();

        connection.rollback();
        connection.setAutoCommit(true);

        statement = connection.createStatement();

        resultSet = statement.executeQuery(SELECT_COUNT_FROM_ONE);
        resultSet.next();

        assertEquals(0, resultSet.getLong(1));

        statement.close();
    }

    @Test
    public void updateRowCommit() throws SQLException {
        connection.setAutoCommit(false);

        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        statement.execute(INSERT_INTO_ONE);

        ResultSet resultSet = statement.executeQuery("SELECT id, value FROM one WHERE id=1");
        resultSet.next();

        assertEquals(1, resultSet.getLong(1));
        assertEquals("1", resultSet.getString(2));

        resultSet.updateString(2, "updatedRow");
        resultSet.updateRow();


        resultSet = statement.executeQuery("SELECT id, value FROM one WHERE id=1");
        resultSet.next();

        assertEquals(1, resultSet.getLong(1));
        assertEquals("updatedRow", resultSet.getString(2));

        connection.commit();

        resultSet = statement.executeQuery("SELECT id, value FROM one WHERE id=1");
        resultSet.next();

        assertEquals(1, resultSet.getLong(1));
        assertEquals("updatedRow", resultSet.getString(2));



        statement.close();
        connection.setAutoCommit(true);
    }

    @Test
    public void updateRowRollbackToCheckPoint() throws SQLException {
        connection.setAutoCommit(false);

        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        statement.execute(INSERT_INTO_ONE);

        ResultSet resultSet = statement.executeQuery("SELECT id, value FROM one WHERE id=1");

        resultSet.next();
        assertEquals(1, resultSet.getLong(1));
        assertEquals("1", resultSet.getString(2));

        resultSet.updateString(2, "updatedRow");
        resultSet.updateRow();

        Savepoint first = connection.setSavepoint("first");

        resultSet = statement.executeQuery("SELECT id, value FROM one WHERE id=2");

        resultSet.next();
        assertEquals(2, resultSet.getLong(1));
        assertEquals("2", resultSet.getString(2));

        resultSet.updateString(2, "updateSecondRow");
        resultSet.updateRow();

        resultSet = statement.executeQuery("SELECT id, value FROM one WHERE id=2");
        resultSet.next();

        assertEquals(2, resultSet.getLong(1));
        assertEquals("updateSecondRow", resultSet.getString(2));

        connection.rollback(first);
        connection.commit();

        resultSet = statement.executeQuery("SELECT id, value FROM one WHERE id in (1, 2) ORDER BY id ASC ");

        resultSet.next();
        assertEquals(1, resultSet.getLong(1));
        assertEquals("updatedRow", resultSet.getString(2));

        resultSet.next();
        assertEquals(2, resultSet.getLong(1));
        assertEquals("2", resultSet.getString(2));

        statement.close();
        connection.setAutoCommit(true);
    }
}
