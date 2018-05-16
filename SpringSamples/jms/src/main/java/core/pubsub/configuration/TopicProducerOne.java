package core.pubsub.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TopicProducerOne {
    private static final Logger logger = LoggerFactory.getLogger("TopicProducerOne");

    private final JmsTemplate jmsTemplate;

    @Autowired
    public TopicProducerOne(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void send() {
        logger.info("Send message to topic1");
        jmsTemplate.convertAndSend("topic1", "Some message to topic1");
    }
}
