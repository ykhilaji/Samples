package pure;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class TopicSample {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Producer producer = new Producer();
        Consumer consumerOne = new Consumer(1, "queue1", "topic.a.b");
        Consumer consumerTwo = new Consumer(2, "queue2", "topic.b.c");
        Consumer consumerThree = new Consumer(3, "queue3", "#");
        Consumer consumerFour = new Consumer(4, "queue4", "*.a.*");

        String[] keys = new String[]{"topic.a.b", "topic.b.c", "topic.discard.a", "random.topic", "topic.a"};
        Random random = new Random();

        for (int i = 0; i < 20; ++i) {
            producer.send(String.format("Message #%d", i), keys[random.nextInt(keys.length)]);
            Thread.sleep(100);
        }

        producer.close();
        consumerOne.close();
        consumerTwo.close();
        consumerThree.close();
        consumerFour.close();


        /*
        Routing key: topic.a.b Message: Message #0
        Consumer [3][queue3][#]: Message #0
        Consumer [1][queue1][topic.a.b]: Message #0
        Consumer [4][queue4][*.a.*]: Message #0
        Routing key: topic.discard.a Message: Message #1
        Consumer [3][queue3][#]: Message #1
        Routing key: random.topic Message: Message #2
        Consumer [3][queue3][#]: Message #2
        Routing key: topic.discard.a Message: Message #3
        Consumer [3][queue3][#]: Message #3
        Routing key: random.topic Message: Message #4
        Consumer [3][queue3][#]: Message #4
        Routing key: topic.discard.a Message: Message #5
        Consumer [3][queue3][#]: Message #5
        Routing key: topic.a Message: Message #6
        Consumer [3][queue3][#]: Message #6
        Routing key: topic.a Message: Message #7
        Consumer [3][queue3][#]: Message #7
        Routing key: topic.b.c Message: Message #8
        Consumer [3][queue3][#]: Message #8
        Consumer [2][queue2][topic.b.c]: Message #8
        Routing key: topic.discard.a Message: Message #9
        Consumer [3][queue3][#]: Message #9
        Routing key: random.topic Message: Message #10
        Consumer [3][queue3][#]: Message #10
        Routing key: topic.discard.a Message: Message #11
        Consumer [3][queue3][#]: Message #11
        Routing key: topic.discard.a Message: Message #12
        Consumer [3][queue3][#]: Message #12
        Routing key: topic.a.b Message: Message #13
        Consumer [3][queue3][#]: Message #13
        Consumer [4][queue4][*.a.*]: Message #13
        Consumer [1][queue1][topic.a.b]: Message #13
        Routing key: random.topic Message: Message #14
        Consumer [3][queue3][#]: Message #14
        Routing key: topic.b.c Message: Message #15
        Consumer [3][queue3][#]: Message #15
        Consumer [2][queue2][topic.b.c]: Message #15
        Routing key: topic.discard.a Message: Message #16
        Consumer [3][queue3][#]: Message #16
        Routing key: topic.a Message: Message #17
        Consumer [3][queue3][#]: Message #17
        Routing key: topic.a.b Message: Message #18
        Consumer [3][queue3][#]: Message #18
        Consumer [1][queue1][topic.a.b]: Message #18
        Consumer [4][queue4][*.a.*]: Message #18
        Routing key: topic.a.b Message: Message #19
        Consumer [4][queue4][*.a.*]: Message #19
        Consumer [1][queue1][topic.a.b]: Message #19
        Consumer [3][queue3][#]: Message #19
        * */
    }


    static class Producer {
        private Connection connection;
        private Channel channel;

        public Producer() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            boolean durable = false;
            boolean temporary = false;
            boolean exclusive = false;

            String exchangeName = "topicMessages";
            String exchangeType = "topic";

            this.channel.exchangeDeclare(exchangeName, exchangeType);
        }

        public void send(String message, String routingKey) throws IOException {
            System.out.println(String.format("Routing key: %s Message: %s", routingKey, message));
            // First param: exchange name
            this.channel.basicPublish("topicMessages", routingKey, null, message.getBytes());
        }

        public void close() throws IOException, TimeoutException {
            this.channel.close();
            this.connection.close();
        }
    }

    static class Consumer {
        private Connection connection;
        private Channel channel;
        private String queueName;

        public Consumer(final int id, final String queueName, final String routingKey) throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            this.queueName = queueName;

            boolean durable = false;
            boolean temporary = false;
            boolean exclusive = false;

            this.channel.queueDeclare(queueName, durable, temporary, exclusive, null);

            String exchangeName = "topicMessages";
            this.channel.queueBind(queueName, exchangeName, routingKey);

            boolean autoAck = false;

            this.channel.basicConsume(queueName, autoAck, new DefaultConsumer(this.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(String.format("Consumer [%d][%s][%s]: %s", id, queueName, routingKey, message));

                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });

//            this.channel.basicQos(1);
        }

        public void close() throws IOException, TimeoutException {
            this.channel.queueDelete(queueName);
            this.channel.close();
            this.connection.close();
        }
    }
}
