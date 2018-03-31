package camelrabbitmq.topic;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class TopicProducer {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();

        RabbitMQComponent rabbitMQComponent = new RabbitMQComponent(camelContext);

        rabbitMQComponent.setAddresses("localhost");
        rabbitMQComponent.setAutoAck(true);
        rabbitMQComponent.setDeclare(true);
        rabbitMQComponent.setDurable(false);

        camelContext.addComponent("rabbitmq", rabbitMQComponent);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:simple?period=1000").setBody().simple("-> log.debug")
                        .setHeader("rabbitmq.ROUTING_KEY", constant("msg.log.debug"))
                        .to("rabbitmq:sample?exchangeType=topic");

                from("timer:simple?period=2000").setBody().simple("-> api.debug")
                        .setHeader("rabbitmq.ROUTING_KEY", constant("msg.api.debug"))
                        .to("rabbitmq:sample?exchangeType=topic");

                from("timer:simple?period=3000").setBody().simple("-> internal.info")
                        .setHeader("rabbitmq.ROUTING_KEY", constant("msg.internal.info"))
                        .to("rabbitmq:sample?exchangeType=topic");
            }
        });

        camelContext.start();
    }
}
