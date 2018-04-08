package basic.topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ProducerTopic {
    private TopicConnection connection;
    private TopicSession session;
    private Topic topic;
    private TopicPublisher publisher;

    public ProducerTopic(ActiveMQConnectionFactory connectionFactory, String topicName) throws JMSException {
        connection = connectionFactory.createTopicConnection();
        connection.setExceptionListener(Throwable::printStackTrace);
        connection.start();

        session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        topic = createTopic(topicName);
        publisher = createTopicPublisher();
    }

    private Topic createTopic(String topic) throws JMSException {
        return session.createTopic(topic);
    }

    private TopicPublisher createTopicPublisher() throws JMSException {
        return session.createPublisher(topic);
    }

    public void publish(String body) throws JMSException {
        publish(session.createTextMessage(body));
    }

    public void publish(TextMessage message) throws JMSException {
        publisher.publish(message);
    }

    public void close() throws JMSException {
        publisher.close();
        session.close();
        connection.close();
    }
}
