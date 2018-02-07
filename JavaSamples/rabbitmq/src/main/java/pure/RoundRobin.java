package pure;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RoundRobin {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Consumer consumerOne = new Consumer(1, 1);
        Consumer consumerTwo = new Consumer(2, 1);
        Producer producer = new Producer();

        for (int i = 0; i < 10; ++i) {
            producer.send(String.format("Message #%d", i));
            Thread.sleep(100);
        }

        producer.close();
        consumerOne.close();
        consumerTwo.close();

        /*
        *   Consumer [1]: Message #0
            Consumer [2]: Message #1
            Consumer [1]: Message #2
            Consumer [2]: Message #3
            Consumer [1]: Message #4
            Consumer [2]: Message #5
            Consumer [1]: Message #6
            Consumer [2]: Message #7
            Consumer [1]: Message #8
            Consumer [2]: Message #9
*/
    }

    static class Producer {
        private Connection connection;
        private Channel channel;

        public Producer() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            boolean durable = true;
            boolean temporary = false;
            boolean exclusive = false;

            this.channel.queueDeclare("example", durable, temporary, exclusive, null);
        }

        public void send(String message) throws IOException {
            this.channel.basicPublish("", "example", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        }

        public void close() throws IOException, TimeoutException {
            this.channel.close();
            this.connection.close();
        }
    }

    static class Consumer {
        private Connection connection;
        private Channel channel;

        public Consumer(final int id, int maxMessagesInQueue) throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            boolean durable = true;
            boolean temporary = false;
            boolean exclusive = false;

            this.channel.queueDeclare("example", durable, temporary, exclusive, null);

            boolean autoAck = false;

            this.channel.basicConsume("example", autoAck, new DefaultConsumer(this.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(String.format("Consumer [%d]: %s", id, message));

                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });

            this.channel.basicQos(maxMessagesInQueue);
        }

        public void close() throws IOException, TimeoutException {
            this.channel.queueDelete("example");
            this.channel.close();
            this.connection.close();
        }
    }
}
