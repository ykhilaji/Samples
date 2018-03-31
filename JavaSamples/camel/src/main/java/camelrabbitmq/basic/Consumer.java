package camelrabbitmq.basic;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class Consumer {
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
                from("rabbitmq:sample?queue=camel")
                        .to("stream:out");
            }
        });

        camelContext.start();
    }
}
