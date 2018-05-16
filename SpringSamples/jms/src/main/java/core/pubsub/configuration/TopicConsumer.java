package core.pubsub.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

@Component
public class TopicConsumer {
    private static final Logger logger = LoggerFactory.getLogger("consumer");

    @JmsListener(destination = "topic1")
    public void receiveTopic1(TextMessage message) throws JMSException {
        logger.info(String.format("Message from topic1: %s", message.getText()));
    }

    @JmsListener(destination = "topic2")
    public void receiveTopic2(TextMessage message) throws JMSException {
        logger.info(String.format("Message from topic2: %s", message.getText()));
    }
}
