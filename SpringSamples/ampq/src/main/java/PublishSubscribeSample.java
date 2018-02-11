import org.springframework.amqp.core.*;
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

@Configuration
@EnableRabbit
public class PublishSubscribeSample {
    @Bean(name = "queue1")
    public Queue queue1() {
        boolean durable = false;
        boolean exclusive = false;
        boolean autoDelete = true;
        return new Queue("queue1", durable, exclusive, autoDelete);
    }

    @Bean(name = "queue2")
    public Queue queue2() {
        boolean durable = false;
        boolean exclusive = false;
        boolean autoDelete = true;
        return new Queue("queue2", durable, exclusive, autoDelete);
    }

    @Bean
    public FanoutExchange exchange(){
        return new FanoutExchange("sampleExchange");
    }

    @Bean
    @Autowired
    public Binding binding1(FanoutExchange exchange){
        return BindingBuilder.bind(queue1()).to(exchange);
    }

    @Bean
    @Autowired
    public Binding binding2(FanoutExchange exchange){
        return BindingBuilder.bind(queue2()).to(exchange);
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
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setExchange("sampleExchange");

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
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PublishSubscribeSample.class);
        Producer producer = context.getBean(Producer.class);
        Consumer consumer = context.getBean(Consumer.class);

        for(int i = 0; i < 10; ++i) {
            producer.send(i);
        }
    }

    class Producer {
        @Autowired
        private RabbitTemplate template;

        public void send(int i) {
            String message = String.format("message №%d", i);
            this.template.convertAndSend(message);
            System.out.println(String.format("Sent message: %s", message));
        }
    }

    public class Consumer {
        @RabbitListener(queues = "queue1")
        public void worker1(String message) {
            System.out.println(String.format("Worker [queue1]: %s", message));
        }

        @RabbitListener(queues = "queue2")
        public void worker2(String message) {
            System.out.println(String.format("Worker [queue2]: %s", message));
        }

    }
}
