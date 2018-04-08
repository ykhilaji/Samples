package basic.topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ConsumerWithListener implements MessageListener {
    private TopicConnection connection;
    private TopicSession session;
    private Topic topic;
    private TopicSubscriber subscriber;
    private long id;

    public ConsumerWithListener(ActiveMQConnectionFactory connectionFactory, String topicName, long id) throws JMSException {
        this.id = id;

        connection = connectionFactory.createTopicConnection();
        connection.setExceptionListener(Throwable::printStackTrace);
        connection.start();

        session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        topic = createTopic(topicName);

        subscriber = createTopicSubscriber();
        subscriber.setMessageListener(this);
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                System.out.println(String.format("[ConsumerWithListener #%d] Received message: %s", id, ((TextMessage) message).getText()));
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private Topic createTopic(String topic) throws JMSException {
        return session.createTopic(topic);
    }

    private TopicSubscriber createTopicSubscriber() throws JMSException {
        return session.createSubscriber(topic);
    }

    public void close() throws JMSException {
        subscriber.close();
        session.close();
        connection.close();
    }
}
