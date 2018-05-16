package core.withreply.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReplyProducer {
    private final JmsTemplate jmsTemplate;

    @Autowired
    public ReplyProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void send() {
        jmsTemplate.convertAndSend("sampleQueue", "Some message");
    }
}
