package basicsample.model;

import basicsample.model.entity.User;

public interface UserDAO {
    User insert(User user);
    boolean delete(long id);
    User select(long id);
    Iterable<User> selectAll();
    User update(User user);
}
