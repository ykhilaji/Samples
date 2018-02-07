package pure;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class PublishSubscribe {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Producer producer = new Producer();
        Consumer consumerOne = new Consumer(1, "firstQueue");
        Consumer consumerTwo = new Consumer(2, "secondQueue");

        for (int i = 0; i < 10; ++i) {
            producer.send(String.format("Message #%d", i));
            Thread.sleep(500);
        }

        producer.close();
        consumerOne.close();
        consumerTwo.close();

        /*
        *   Consumer [1][firstQueue]: Message #0
            Consumer [2][secondQueue]: Message #0
            Consumer [1][firstQueue]: Message #1
            Consumer [2][secondQueue]: Message #1
            Consumer [2][secondQueue]: Message #2
            Consumer [1][firstQueue]: Message #2
            Consumer [1][firstQueue]: Message #3
            Consumer [2][secondQueue]: Message #3
            Consumer [1][firstQueue]: Message #4
            Consumer [2][secondQueue]: Message #4
            Consumer [1][firstQueue]: Message #5
            Consumer [2][secondQueue]: Message #5
            Consumer [1][firstQueue]: Message #6
            Consumer [2][secondQueue]: Message #6
            Consumer [1][firstQueue]: Message #7
            Consumer [2][secondQueue]: Message #7
            Consumer [2][secondQueue]: Message #8
            Consumer [1][firstQueue]: Message #8
            Consumer [1][firstQueue]: Message #9
            Consumer [2][secondQueue]: Message #9
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

            String exchangeName = "exchange";
            String exchangeType = "fanout"; // direct, fanout, topic or header

            this.channel.exchangeDeclare(exchangeName, exchangeType);
            this.channel.queueDeclare("example", durable, temporary, exclusive, null);
        }

        public void send(String message) throws IOException {
            // First param: Exchange name
            // Second param: Routing key
            this.channel.basicPublish("exchange", "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
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

        public Consumer(final int id, final String queueName) throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            this.queueName = queueName;

            boolean durable = true;
            boolean temporary = false;
            boolean exclusive = false;

            this.channel.queueDeclare(queueName, durable, temporary, exclusive, null);

            String exchangeName = "exchange";
            String routingKey = "";
            this.channel.queueBind(queueName, exchangeName, routingKey);

            boolean autoAck = false;

            this.channel.basicConsume(queueName, autoAck, new DefaultConsumer(this.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(String.format("Consumer [%d][%s]: %s", id, queueName, message));

                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });

            int maxMessagesAtOneTime = 1;
            this.channel.basicQos(maxMessagesAtOneTime);
        }

        public void close() throws IOException, TimeoutException {
            this.channel.queueDelete(queueName);
            this.channel.close();
            this.connection.close();
        }
    }
}
