package camelrabbitmq.topic;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class TopicConsumer {
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
                from("rabbitmq:sample?exchangeType=topic&queue=log&routingKey=msg.log.*")
                        .bean(LogProcessor.class, "consume");

                from("rabbitmq:sample?exchangeType=topic&queue=api&routingKey=msg.api.*")
                        .bean(ApiProcessor.class, "consume");

                from("rabbitmq:sample?exchangeType=topic&queue=internal&routingKey=msg.*.info")
                        .bean(CommonProcessor.class, "consume");
            }
        });

        camelContext.start();
    }

    public static class ApiProcessor {
        public void consume(String msg) {
            System.out.println(String.format("API: %s", msg));
        }
    }

    public static class LogProcessor {
        public void consume(String msg) {
            System.out.println(String.format("Log: %s", msg));
        }
    }

    public static class CommonProcessor {
        public void consume(String msg) {
            System.out.println(String.format("Common: %s", msg));
        }
    }
}
