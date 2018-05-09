package CassandraRepository;

import CassandraRepository.configuration.CassandraConfiguration;
import CassandraRepository.model.Entity;
import CassandraRepository.model.Key;
import CassandraRepository.repository.CassandraRepositoryImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.cassandra.core.CassandraOperations;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

public class Cassandra {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(CassandraConfiguration.class);

        CassandraRepositoryImpl repository = context.getBean(CassandraRepositoryImpl.class);
        CassandraOperations operations = context.getBean(CassandraOperations.class);

        Key key1 = new Key(1L, "1");
        Entity entity1 = new Entity(key1, "val1");

        Key key2 = new Key(1L, "2");
        Entity entity2 = new Entity(key2, "val2");

        Key key3 = new Key(1L, "3");
        Entity entity3 = new Entity(key3, "val3");

        repository.save(entity1);
        System.out.println(repository.findById(key1).orElseGet(null));
        repository.deleteById(key1);
        System.out.println(repository.existsById(key1));

        operations.batchOps()
                .insert(entity1, entity2, entity3)
                .execute();

        operations.select(query(where("row_id").is(1L)), Entity.class).forEach(System.out::println);
        System.out.println();
        operations.select(query(where("row_id").is(1L)).and(where("field").is("val3")).withAllowFiltering(), Entity.class).forEach(System.out::println);

        System.exit(0);
    }
}
