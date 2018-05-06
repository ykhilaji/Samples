import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.*;

public class CassandraDriverMappingSample {
    public static void main(String[] args) {
        Cluster cluster = Cluster
                .builder()
                .addContactPoint("192.168.99.100")
                .withPort(9042)
                .build();

        try (Session session = cluster.connect()) {
            session.execute("create keyspace if not exists sample with" +
                    " replication = {'class': 'SimpleStrategy', 'replication_factor':1} " +
                    " and durable_writes = true;");
            session.execute("create table if not exists sample.user " +
                    "(id int, first_name text, last_name text, age int, primary key ((id), first_name));");

            MappingManager mappingManager = new MappingManager(session);
            Mapper<User> mapper = mappingManager.mapper(User.class);
            mapper.setDefaultSaveOptions(Mapper.Option.ttl(10));

            System.out.println(mapper.get(1, "some_name")); // null
            mapper.save(new User(1, "fistName", "lastName", 10));
            System.out.println(mapper.get(1, "some_name")); // null
            System.out.println(mapper.get(1, "fistName")); // User{id=1, firstName='fistName', lastName='lastName', age=10}
            mapper.delete(1, "fistName");
            System.out.println(mapper.get(1, "fistName")); // User{id=1, firstName='fistName', lastName='lastName', age=10}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cluster.close();
        }
    }

    @Table(keyspace = "sample", name = "user")
    public static class User {
        @PartitionKey
        @Column(name = "id")
        private int id;
        @ClusteringColumn
        @Column(name = "first_name")
        private String firstName;
        @Column(name = "last_name")
        private String lastName;
        @Column(name = "age", codec = Defaults.NoCodec.class) // NoCodec.class - default
        private int age;

        public User() {
        }

        public User(int id, String firstName, String lastName, int age) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
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

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            if (id != user.id) return false;
            if (age != user.age) return false;
            if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) return false;
            return lastName != null ? lastName.equals(user.lastName) : user.lastName == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
            result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
            result = 31 * result + age;
            return result;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
