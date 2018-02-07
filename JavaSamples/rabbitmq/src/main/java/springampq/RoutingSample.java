package springampq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;


@Configuration
@EnableRabbit
public class RoutingSample {
    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public RabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory listenerContainerFactory = new SimpleRabbitListenerContainerFactory();

        listenerContainerFactory.setConcurrentConsumers(1);
        listenerContainerFactory.setConnectionFactory(connectionFactory());

        return listenerContainerFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate((connectionFactory()));
        template.setExchange("sampleDirectExchange");
        return template;
    }

    @Bean
    public Producer producer() {
        return new Producer();
    }

    @Bean
    public Consumer consumer() {
        return new Consumer();
    }

    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RoutingSample.class);
        Producer producer = context.getBean(Producer.class);
        Consumer consumer = context.getBean(Consumer.class);

        for(int i = 0; i < 10; ++i) {
            producer.send(i);
        }
    }

    class Producer {
        @Autowired
        private RabbitTemplate template;
        String[] keys = new String[]{"key1", "key2"};
        Random random = new Random();

        public void send(int i) {
            String key = keys[random.nextInt(keys.length)];
            String message = String.format("message №%d Key: %s", i, key);
            this.template.convertAndSend(key, message);
            System.out.println(String.format("Sent message: %s", message));
        }
    }

    public class Consumer {
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "queue1", durable = "false", autoDelete = "true"),
                exchange = @Exchange(value = "sampleDirectExchange", autoDelete = "true", ignoreDeclarationExceptions = "true"),
                key = "key1")
        )
        public void worker1(String message) {
            System.out.println(String.format("Worker [queue1]: %s", message));
        }

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "queue2", durable = "false", autoDelete = "true"),
                exchange = @Exchange(value = "sampleDirectExchange", autoDelete = "true", ignoreDeclarationExceptions = "true"),
                key = "key2")
        )
        public void worker2(String message) {
            System.out.println(String.format("Worker [queue2]: %s", message));
        }

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "queue3", durable = "false", autoDelete = "true"),
                exchange = @Exchange(value = "sampleDirectExchange", autoDelete = "true", ignoreDeclarationExceptions = "true"),
                key = "key1")
        )
        public void worker3(String message) {
            System.out.println(String.format("Worker [queue3]: %s", message));
        }
    }
}