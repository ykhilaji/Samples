package basic.direct;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Consumer implements Runnable {
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer consumer;
    private long id;

    private boolean isRunning = true;

    public Consumer(ActiveMQConnectionFactory connectionFactory, String topic, long id) throws JMSException {
        connection = connectionFactory.createConnection();
        connection.setExceptionListener(Throwable::printStackTrace);
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = createDestination(topic);
        consumer = createMessageConsumer();

        this.id = id;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Message message = consumer.receive(1000);
                if (message instanceof TextMessage) {
                    System.out.println(String.format("[ConsumerWithListener #%d] Received message: %s", id, ((TextMessage) message).getText()));
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private Destination createDestination(String topic) throws JMSException {
        return session.createQueue(topic);
    }

    private MessageConsumer createMessageConsumer() throws JMSException {
        return session.createConsumer(destination);
    }

    public void close() throws JMSException {
        isRunning = false;
        consumer.close();
        session.close();
        connection.close();
    }
}
