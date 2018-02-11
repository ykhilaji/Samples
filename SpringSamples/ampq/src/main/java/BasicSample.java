import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class BasicSample {
    @Bean
    public Queue sample() {
        boolean durable = false;
        boolean exclusive = false;
        boolean autoDelete = true;
        return new Queue("sample", durable, exclusive, autoDelete);
    }

//    @Bean
//    public BatchingStrategy batchingStrategy() {
//        // @Autowired BatchingRabbitTemplate
//        return new SimpleBatchingStrategy(10, 10, 100);
//    }


    @Bean
    public ConnectionFactory connectionFactory() {
//        connectionFactory.addConnectionListener(new ConnectionListener() {
//
//            @Override
//            public void onCreate(Connection connection) {
//            }
//
//            @Override
//            public void onShutDown(ShutdownSignalException signal) {
//        ...
//            }

        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
//        RetryTemplate retryTemplate = new RetryTemplate();
//        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
//        backOffPolicy.setInitialInterval(500);
//        backOffPolicy.setMultiplier(10.0);
//        backOffPolicy.setMaxInterval(10000);
//        retryTemplate.setBackOffPolicy(backOffPolicy);
//        template.setRetryTemplate(retryTemplate);
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

    @Bean
    MessageListenerAdapter listenerAdapter(Consumer receiver) {
//        new MessageListenerAdapter((ReplyingMessageListener<String, String>) data -> {
//            return result;
//        }));
        return new MessageListenerAdapter(receiver, "receive");
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("sample");
        container.setMessageListener(listenerAdapter);
        return container;
    }

//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory());
//        factory.setConcurrentConsumers(3);
//        factory.setMaxConcurrentConsumers(10);
//        return factory;
//    }


//    @Bean
//    public MessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }

//    @Bean
//    public TopicExchange marketDataExchange() {
//        return new TopicExchange("app.stock.marketdata");
//    }

//    @Bean
//    public Queue stockRequestQueue() {
//        return new Queue("app.stock.request");
//    }

//    @Bean
//    public Binding marketDataBinding() {
//        return BindingBuilder.bind(
//                marketDataQueue()).to(marketDataExchange()).with(marketDataRoutingKey);
//    }

//    @Bean
//    public RabbitTemplate rabbitTemplate() {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory());
//        template.setMessageConverter(jsonMessageConverter());
//        configureRabbitTemplate(template);
//        return template;
//    }

//    @Bean
//    public List<Exchange> es() {
//        return Arrays.<Exchange>asList(
//                new DirectExchange("e2", false, true),
//                new DirectExchange("e3", false, true)
//        );
//    }
//
//    @Bean
//    public List<Queue> qs() {
//        return Arrays.asList(
//                new Queue("q2", false, false, true),
//                new Queue("q3", false, false, true)
//        );
//    }
//
//    @Bean
//    public List<Binding> bs() {
//        return Arrays.asList(
//                new Binding("q2", DestinationType.QUEUE, "e2", "k2", null),
//                new Binding("q3", DestinationType.QUEUE, "e3", "k3", null)
//        );
//    }
//
//    @Bean
//    public List<Declarable> ds() {
//        return Arrays.<Declarable>asList(
//                new DirectExchange("e4", false, true),
//                new Queue("q4", false, false, true),
//                new Binding("q4", DestinationType.QUEUE, "e4", "k4", null)
//        );
//    }

    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BasicSample.class);
        Producer producer = context.getBean(Producer.class);
        Consumer consumer = context.getBean(Consumer.class);

        producer.send();
    }

    class Producer {
        @Autowired
        private RabbitTemplate template;

        @Autowired
        private Queue queue;

        @Scheduled(fixedDelay = 1000, initialDelay = 500)
        public void send() {
            String message = "message";
//            this.template.setExchange("");
//            this.template.setRoutingKey("");
//            this.template.send("exchange", "routing key", new Message("body".getBytes(), MessageProperties.CONTENT_TYPE_BYTES));
            this.template.convertAndSend(queue.getName(), message);
            System.out.println(" [x] Sent '" + message + "'");
        }
    }

    @RabbitListener(queues = "sample")
    class Consumer {
        @RabbitHandler
        public void receive(String in) {
            System.out.println(" [x] Received '" + in + "'");
        }
    }
}


//@Component
//public class MyService {
//
//    @RabbitListener(queues = { "queue1", "queue2" } )
//    public void processOrder(String data, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
//        ...
//    }
//
//}