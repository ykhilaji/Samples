package camelrabbitmq.routing;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class Producer {
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
                from("timer:simple?period=1000").setBody().simple("-> one")
                        .to("rabbitmq:sampledirect?exchangeType=direct&routingKey=one");

                from("timer:simple?period=1000").setBody().simple("-> two")
                        .to("rabbitmq:sampledirect?exchangeType=direct&routingKey=two");
            }
        });

        camelContext.start();
    }
}
