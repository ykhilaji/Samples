package guice.camel.kafka;

import org.apache.camel.ProducerTemplate;

import javax.inject.Inject;

public class Producer {
    @Inject
    ProducerTemplate template;

    public void send() {

    }
}
