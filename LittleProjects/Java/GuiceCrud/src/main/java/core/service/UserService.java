package core.service;

import com.google.inject.Inject;
import core.aop.Log;
import core.model.User;
import core.repository.UserRepository;

public class UserService {
    @Inject
    private UserRepository repository;

    public UserService() {
    }

    @Log
    public User get(String email) {
        System.out.println(repository);
        return repository.get(email);
    }

    @Log
    public User update(User user) {
        return repository.update(user);
    }

    @Log
    public User delete(String email) {
        return repository.delete(email);
    }

    @Log
    public User save(User user) {
        return repository.save(user);
    }
}
