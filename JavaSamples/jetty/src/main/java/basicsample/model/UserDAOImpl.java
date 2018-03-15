package basicsample.model;

import basicsample.model.entity.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    private DataBase dataBase;

    public UserDAOImpl(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    public User insert(User user) {
        try {
            PreparedStatement statement = dataBase.preparedStatement("insert into jetty.user values (?, ?, ?)");
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.executeQuery();

            dataBase.commit();
        } catch (SQLException e) {
            dataBase.rollback();
            e.printStackTrace();
        }
        return user;
    }

    public boolean delete(long aLong) {
        try {
            PreparedStatement statement = dataBase.preparedStatement("delete from jetty.user where id = ?");
            statement.setLong(1, aLong);
            statement.executeQuery();
            dataBase.commit();

            return statement.getUpdateCount() > 0;
        } catch (SQLException e) {
            dataBase.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public User select(long aLong) {
        User user = User.anonymous;

        try {
            PreparedStatement statement = dataBase.preparedStatement("select * from jetty.user where id = ?");
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                user = new User(resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public Iterable<User> selectAll() {
        List<User> users = new ArrayList<>();
        try {
            Statement statement = dataBase.statement();
            ResultSet resultSet = statement.executeQuery("select * from jetty.user");

            while (resultSet.next()) {
                users.add(new User(resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public User update(User user) {
        try {
            PreparedStatement statement = dataBase.updatableStatement("select * from jetty.user where id = ?");
            statement.setLong(1, user.getId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getLong(1) == user.getId()) {
                    resultSet.updateString(2, user.getFirstName());
                    resultSet.updateString(3, user.getLastName());
                    resultSet.updateString(4, user.getEmail());
                    resultSet.updateRow();
                }
            }

            dataBase.commit();
        } catch (SQLException e) {
            dataBase.rollback();
            e.printStackTrace();
        }

        return user;
    }
}
