import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.*;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;

public class CassandraAccessorSample {
    public static void main(String[] args) {
        Cluster cluster = Cluster
                .builder()
                .addContactPoint("192.168.99.100")
                .withPort(9042)
                .build();

        try (Session session = cluster.connect()) {
            Map<String, Object> props = new HashMap<>();
            props.put("replication_factor", 1);
            props.put("class", "SimpleStrategy");

            session.execute(SchemaBuilder
                    .createKeyspace("sample")
                    .ifNotExists()
                    .with()
                    .replication(props));

            session.execute(SchemaBuilder
                    .createTable("sample", "user")
                    .ifNotExists()
                    .addPartitionKey("id", DataType.cint())
                    .addClusteringColumn("firstName", DataType.text())
                    .addColumn("lastName", DataType.text())
                    .addColumn("email", DataType.text()));

            session.execute(SchemaBuilder.createIndex("userEmail")
                    .ifNotExists()
                    .onTable("sample", "user")
                    .andColumn("email"));

            session.execute(QueryBuilder.truncate("sample", "user"));

            MappingManager manager = new MappingManager(session);
            Mapper<User> userMapper = manager.mapper(User.class);
            UserAccessor accessor = manager.createAccessor(UserAccessor.class);

            userMapper.save(new User(1, "name1", "lname1", "email1"));
            userMapper.save(new User(2, "name1", "lname2", "email2"));
            userMapper.save(new User(3, "name2", "lname1", "email3"));
            userMapper.save(new User(4, "name2", "lname3", "email4"));
            userMapper.save(new User(5, "name3", "lname2", "email5"));

            System.out.println(userMapper.get(1, "name1"));
            System.out.println(userMapper.get(2, "name1"));
            System.out.println(userMapper.get(3, "name1")); // null

            accessor
                    .selectByEmail("email1")
                    .all()
                    .forEach(System.out::println);

            accessor
                    .selectByLastName("lname3")
                    .all()
                    .forEach(System.out::println);

            accessor
                    .selectByEmailAsync("email2")
                    .get()
                    .all()
                    .forEach(System.out::println);

            accessor
                    .selectByEmailJson("email2")
                    .all()
                    .forEach(System.out::println);

            session.execute(SchemaBuilder
                    .dropIndex("sample", "userEmail")
                    .ifExists());

            session.execute(SchemaBuilder
                    .dropTable("sample", "user")
                    .ifExists());

            session.execute(SchemaBuilder
                    .dropKeyspace("sample")
                    .ifExists());

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cluster.close();
        }
    }

    @Accessor
    interface UserAccessor {
        @Query("select * from sample.user where email = ?")
        Result<User> selectByEmail(String email);

        @Query("select json * from sample.user where email = ?")
        ResultSet selectByEmailJson(String email);

        @Query("select * from sample.user where email = :e")
        ListenableFuture<Result<User>> selectByEmailAsync(@Param("e") String email);

        @Query("select * from sample.user where lastName = ? allow filtering")
        Result<User> selectByLastName(String lastName);
    }

    @Table(keyspace = "sample", name = "user")
    public static class User {
        @PartitionKey
        @Column(name = "id")
        private int id;
        @ClusteringColumn
        @Column(name = "firstName")
        private String firstName;
        @Column(name = "lastName")
        private String lastName;
        @Column(name = "email")
        private String email;

        public User() {
        }

        public User(int id, String firstName, String lastName, String email) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}
