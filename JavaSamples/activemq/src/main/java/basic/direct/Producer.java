package basic.direct;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Producer {
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;

    public Producer(ActiveMQConnectionFactory connectionFactory, String topic) throws JMSException {
        connection = connectionFactory.createConnection();
        connection.setExceptionListener(Throwable::printStackTrace);
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = createDestination(topic);
        producer = createMessageProducer();
    }

    private Destination createDestination(String topic) throws JMSException {
        return session.createQueue(topic);
    }

    private MessageProducer createMessageProducer() throws JMSException {
        return session.createProducer(destination);
    }

    public void send(String body) throws JMSException {
        send(session.createTextMessage(body));
    }

    public void send(TextMessage message) throws JMSException {
        producer.send(message);
    }

    public void close() throws JMSException {
        producer.close();
        session.close();
        connection.close();
    }
}
