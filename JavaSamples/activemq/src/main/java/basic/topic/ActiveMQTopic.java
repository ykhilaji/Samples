package basic.topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ActiveMQTopic {
    private static String TOPIC = "TOPIC";

    public static void main(String[] args) throws JMSException, IOException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://192.168.99.100");

        ProducerTopic producerTopic = new ProducerTopic(connectionFactory, TOPIC);
        ConsumerWithListener consumerWithListenerOne = new ConsumerWithListener(connectionFactory, TOPIC, 1);
        ConsumerWithListener consumerWithListenerTwo = new ConsumerWithListener(connectionFactory, TOPIC, 2);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String message = reader.readLine();

        while (!"exit".equals(message)) {
            producerTopic.publish(message);
            message = reader.readLine();
        }

        consumerWithListenerOne.close();
        consumerWithListenerTwo.close();
        producerTopic.close();
    }
}
