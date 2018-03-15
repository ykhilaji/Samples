package basicsample.model;

import java.sql.*;

public class DataBase {
    private static final String URL = "jdbc:postgresql://192.168.99.100:5432/postgres";
    private static final String USER = "postgres";
    private static final String DRIVER = "org.postgresql.Driver";

    private static DataBase instance;

    private Connection connection;

    public static DataBase getInstance() {
        if (instance == null) {
            synchronized (DataBase.class) {
                if (instance == null) {
                    instance = new DataBase();
                }
            }
        }

        return instance;
    }

    private DataBase() {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USER, "");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement preparedStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }

    public Statement statement() throws SQLException {
        return connection.createStatement();
    }

    public PreparedStatement updatableStatement(String query) throws SQLException {
        return connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
