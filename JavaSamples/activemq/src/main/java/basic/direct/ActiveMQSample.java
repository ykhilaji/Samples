package basic.direct;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ActiveMQSample {
    private static String TOPIC = "DIRECT";

    public static void main(String[] args) throws JMSException, IOException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://192.168.99.100");

        Producer producer = new Producer(connectionFactory, TOPIC);
        // RoundRobin
        Consumer consumerOne = new Consumer(connectionFactory, TOPIC, 1);
        Consumer consumerTwo = new Consumer(connectionFactory, TOPIC, 2);

        Thread threadOne = new Thread(consumerOne);
        threadOne.setDaemon(true);
        threadOne.start();

        Thread threadTwo = new Thread(consumerTwo);
        threadTwo.setDaemon(true);
        threadTwo.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String message = reader.readLine();

        while (!"exit".equals(message)) {
            producer.send(message);
            message = reader.readLine();
        }

        consumerOne.close();
        consumerTwo.close();
        producer.close();
    }
}
