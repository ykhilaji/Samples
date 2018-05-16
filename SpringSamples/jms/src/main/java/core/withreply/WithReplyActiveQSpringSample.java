package core.withreply;

import core.withreply.configuration.AppConfiguration;
import core.withreply.configuration.ConsumerAcceptedReply;
import core.withreply.configuration.ReplyConsumer;
import core.withreply.configuration.ReplyProducer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WithReplyActiveQSpringSample {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        ReplyConsumer consumer = context.getBean(ReplyConsumer.class);
        ConsumerAcceptedReply consumerAcceptedReply = context.getBean(ConsumerAcceptedReply.class);
        ReplyProducer producer = context.getBean(ReplyProducer.class);

        Thread.sleep(5000);

        context.close();
    }
}
