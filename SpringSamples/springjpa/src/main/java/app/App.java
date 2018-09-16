package app;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class App {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Configuration.class);
        Repository repository = context.getBean(Repository.class);

        Entity entity = new Entity();
        entity.setValue("value");

        repository.save(entity);
        System.out.println(entity);
        Entity e = repository.find(entity.getId());
        System.out.println(e);
        e.setValue("updated");
        repository.update(e);
        Entity afterUpdate = repository.find(e.getId());
        System.out.println(afterUpdate);

        repository.selectAll();
        repository.delete(afterUpdate);


    }
}
