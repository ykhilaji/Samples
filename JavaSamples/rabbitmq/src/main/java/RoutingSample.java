import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class RoutingSample {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Producer producer = new Producer();
        Consumer consumerOne = new Consumer(1, "queue1", "key1");
        Consumer consumerTwo = new Consumer(2, "queue2", "key2");
        Consumer consumerThree = new Consumer(3, "queue3", "key2");

        String[] keys = new String[]{"key1", "key2"};
        Random random = new Random();

        for (int i = 0; i < 10; ++i) {
            producer.send(String.format("Message #%d", i), keys[random.nextInt(keys.length)]);
            Thread.sleep(100);
        }

        producer.close();
        consumerOne.close();
        consumerTwo.close();
        consumerThree.close();

        /*
        *   Consumer [2][queue2][key2]: Message #0
            Consumer [3][queue3][key2]: Message #0
            Consumer [3][queue3][key2]: Message #1
            Consumer [2][queue2][key2]: Message #1
            Consumer [2][queue2][key2]: Message #2
            Consumer [3][queue3][key2]: Message #2
            Consumer [1][queue1][key1]: Message #3
            Consumer [1][queue1][key1]: Message #4
            Consumer [1][queue1][key1]: Message #5
            Consumer [1][queue1][key1]: Message #6
            Consumer [2][queue2][key2]: Message #7
            Consumer [3][queue3][key2]: Message #7
            Consumer [3][queue3][key2]: Message #8
            Consumer [2][queue2][key2]: Message #8
            Consumer [2][queue2][key2]: Message #9
            Consumer [3][queue3][key2]: Message #9
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

            String exchangeName = "directMessages";
            String exchangeType = "direct"; // direct type - a message goes to the queues whose binding key exactly matches the routing key of the message

            this.channel.exchangeDeclare(exchangeName, exchangeType);
        }

        public void send(String message, String routingKey) throws IOException {
            // First param: exchange name
            this.channel.basicPublish("directMessages", routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
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

            boolean durable = true;
            boolean temporary = false;
            boolean exclusive = false;

            this.channel.queueDeclare(queueName, durable, temporary, exclusive, null);

            String exchangeName = "directMessages";
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

            this.channel.basicQos(1);
        }

        public void close() throws IOException, TimeoutException {
            this.channel.queueDelete(queueName);
            this.channel.close();
            this.connection.close();
        }
    }
}
