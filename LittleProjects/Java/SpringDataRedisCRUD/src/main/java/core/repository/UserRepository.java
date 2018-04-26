package core.repository;

import core.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findByFirstName(String firstName);
    List<User> findByAddress_City(String city);
}
