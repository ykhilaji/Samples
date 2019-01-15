package guice.camel.kafka;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;

import javax.inject.Inject;
import javax.inject.Named;

public class Route extends RouteBuilder {
    @Inject
    Bean bean;
    @Inject
    @Named("from")
    KafkaComponent component;

    public void configure() throws Exception {
        Endpoint endpoint = component.createEndpoint("kafka:from");

        from(endpoint).log("${body}");

        from("timer://guice?delay=1s")
                .setBody(constant("some message"))
                .bean(bean, "send");
    }
}
