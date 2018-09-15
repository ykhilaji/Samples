import app.Configuration;
import app.Entity;
import app.Repository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Configuration.class);
        Repository repository = context.getBean(Repository.class);

        Entity entity = new Entity();
        entity.setId(0);
        entity.setValue("value");

        System.out.println(entity);
        entity = repository.save(entity);
        System.out.println(entity);

        entity.setValue("updatedValue");
        entity = repository.save(entity);
        System.out.println(entity);

        repository.delete(entity);
        entity = repository.findById(1L).orElseGet(Entity::new);
        System.out.println(entity);
    }
}
