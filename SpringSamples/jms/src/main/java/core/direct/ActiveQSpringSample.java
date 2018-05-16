package core.direct;

import core.direct.configuration.AppConfiguration;
import core.direct.configuration.Consumer;
import core.direct.configuration.Producer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ActiveQSpringSample {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        Consumer consumer = context.getBean(Consumer.class);
        Producer producer = context.getBean(Producer.class);

        Thread.sleep(5000);

        context.close();
    }
}
