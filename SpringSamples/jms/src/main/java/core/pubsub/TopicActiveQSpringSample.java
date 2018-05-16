package core.pubsub;

import core.pubsub.configuration.AppConfiguration;
import core.pubsub.configuration.TopicConsumer;
import core.pubsub.configuration.TopicProducerOne;
import core.pubsub.configuration.TopicProducerTwo;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TopicActiveQSpringSample {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        TopicConsumer consumer = context.getBean(TopicConsumer.class);
        TopicProducerOne producerOne = context.getBean(TopicProducerOne.class);
        TopicProducerTwo producerTwo = context.getBean(TopicProducerTwo.class);

        Thread.sleep(5000);

        context.close();
    }
}
