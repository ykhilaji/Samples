package RedisBasic;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class RedisMain {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(RedisConfiguration.class);
        RedisRepository repository = context.getBean(RedisRepository.class);

        repository.save(new Entity(1, "q", "w", "e"));
        repository.save(new Entity(2, "r", "t", "y"));
        repository.save(new Entity(3, "u", "i", "o"));
        repository.save(new Entity(4, "a", "s", "d"));
        repository.save(new Entity(5, "f", "g", "h"));

        repository.findAll().forEach(System.out::println);
    }
}
