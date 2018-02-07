package pure;


import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BasicSample {
    public static void main(String[] args) throws IOException, TimeoutException {
        Consumer consumer = new Consumer();
        Producer producer = new Producer();
        producer.send("Test message");
        producer.close();
        consumer.close();
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

            this.channel.queueDeclare("example", durable, temporary, exclusive, null);
        }

        public void send(String message) throws IOException {
            // Default exchange (~ direct)
            this.channel.basicPublish("", "example", null, message.getBytes());
        }

        public void close() throws IOException, TimeoutException {
            this.channel.close();
            this.connection.close();
        }
    }

    static class Consumer {
        private Connection connection;
        private Channel channel;

        public Consumer() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            boolean durable = false;
            boolean temporary = false;
            boolean exclusive = false;

            this.channel.queueDeclare("example", durable, temporary, exclusive, null);

            boolean autoAck = true;

            this.channel.basicConsume("example", autoAck, new DefaultConsumer(this.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(message);
                }
            });
        }

        public void close() throws IOException, TimeoutException {
            this.channel.queueDelete("example");
            this.channel.close();
            this.connection.close();
        }
    }
}
