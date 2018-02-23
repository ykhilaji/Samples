import java.sql.*;

public class PostgresConnectionSample {
    private static final String URL = "jdbc:postgresql://192.168.99.100:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1111";
    private static final String DRIVER = "org.postgresql.Driver";
    private static final String SELECT = "select * from jdbc.entity where id = ?";

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName(DRIVER);

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = statement.executeQuery("select * from jdbc.entity");

            while (resultSet.next()) {
                System.out.println(String.format("Entity: id: %d, value: %s", resultSet.getInt(1), resultSet.getString(2)));
                if (resultSet.getInt(1) == 3) {
                    resultSet.updateString(2, "updatedValue");
                    resultSet.updateRow();
                }
            }

            statement.close();

            PreparedStatement preparedStatement = connection.prepareStatement(SELECT);
            preparedStatement.setInt(1, 3);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println(String.format("Entity: id: %d, value: %s", resultSet.getInt(1), resultSet.getString(2)));
            }

            preparedStatement.close();
            connection.rollback();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(SELECT);
            preparedStatement.setInt(1, 3);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println(String.format("Entity: id: %d, value: %s", resultSet.getInt(1), resultSet.getString(2)));
            }

            preparedStatement.close();

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
