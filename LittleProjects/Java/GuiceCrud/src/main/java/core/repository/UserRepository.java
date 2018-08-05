package core.repository;

import core.aop.Log;
import core.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private Map<String, User> users;

    public UserRepository() {
        this.users = new HashMap<>();
    }

    @Log
    public User get(String email) {
        return users.getOrDefault(email, new User());
    }

    @Log
    public User update(User user) {
        return users.replace(user.getEmail(), user);
    }

    @Log
    public User delete(String email) {
        return users.remove(email);
    }

    @Log
    public User save(User user) {
        return users.put(user.getEmail(), user);
    }
}
