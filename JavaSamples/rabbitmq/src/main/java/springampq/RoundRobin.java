package springampq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableRabbit
public class RoundRobin {
    @Bean
    public Queue sample() {
        boolean durable = false;
        boolean exclusive = false;
        boolean autoDelete = true;
        return new Queue("sample", durable, exclusive, autoDelete);
    }

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
        return new RabbitTemplate(connectionFactory());
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
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RoundRobin.class);
        Producer producer = context.getBean(Producer.class);
        Consumer consumer = context.getBean(Consumer.class);

        for(int i = 0; i < 10; ++i) {
            producer.send();
        }
    }

    class Producer {
        @Autowired
        private RabbitTemplate template;

        @Autowired
        private Queue queue;

        @Scheduled(fixedDelay = 1000, initialDelay = 500)
        public void send() {
            String message = "message";
            this.template.convertAndSend(queue.getName(), message);
            System.out.println(String.format("Sent message: %s", message));
        }
    }

    public class Consumer {
        @RabbitListener(queues = "sample")
        public void worker1(String message) {
            System.out.println(String.format("Worker [1]: %s", message));
        }

        @RabbitListener(queues = "sample")
        public void worker2(String message) {
            System.out.println(String.format("Worker [2]: %s", message));
        }

    }
}