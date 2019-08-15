package stream.filter.camel;

import com.typesafe.config.Config;
import org.apache.camel.CamelContext;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;

public class FilterRoute extends RouteBuilder {
    private Config config;

    public FilterRoute(Config config) {
        this.config = config;
    }

    public FilterRoute(CamelContext context, Config config) {
        super(context);
        this.config = config;
    }

    @Override
    public void configure() throws Exception {
        String host = config.getString("rabbitmq.host");
        int port = config.getInt("rabbitmq.port");

        String sourceExchange = config.getString("rabbitmq.source.exchange");
        String sourceQueue = config.getString("rabbitmq.source.queue");

        String targetExchange = config.getString("rabbitmq.target.exchange");
        String targetQueue = config.getString("rabbitmq.target.queue");

        String notExistExchange = config.getString("rabbitmq.notExist.exchange");
        String notExistQueue = config.getString("rabbitmq.notExist.queue");

        String failoverExchange = config.getString("rabbitmq.failover.exchange");
        String failoverQueue = config.getString("rabbitmq.failover.queue");

        int batchSize = config.getInt("batch.size");
        int batchTimeout = config.getInt("batch.timeout");

        String source = new StringBuilder()
                .append("rabbitmq://").append(host).append(":").append(port)
                .append("/").append(sourceExchange).append("?queue=").append(sourceQueue)
                .append("&exchangeType=direct").append("&routingKey=").append(sourceQueue)
                .toString();

        String failover = new StringBuilder()
                .append("rabbitmq://").append(host).append(":").append(port)
                .append("/").append(failoverExchange).append("?queue=").append(failoverQueue)
                .append("&exchangeType=direct").append("&routingKey=").append(failoverQueue)
                .toString();

        String target = new StringBuilder()
                .append("rabbitmq://").append(host).append(":").append(port)
                .append("/").append(targetExchange).append("?queue=").append(targetQueue)
                .append("&exchangeType=direct").append("&routingKey=").append(targetQueue)
                .toString();

        String notExist = new StringBuilder()
                .append("rabbitmq://").append(host).append(":").append(port)
                .append("/").append(notExistExchange).append("?queue=").append(notExistQueue)
                .append("&exchangeType=direct").append("&routingKey=").append(notExistQueue)
                .toString();

        onException(TypeConversionException.class)
                .handled(true)
                .log("Incorrect json body: ${body}")
                .setHeader("rabbitmq.ROUTING_KEY", constant(failoverQueue))
                .to(failover);

        onException(NoTypeConversionAvailableException.class)
                .handled(true)
                .log("No type conversion: ${body}")
                .setHeader("rabbitmq.ROUTING_KEY", constant(failoverQueue))
                .to(failover);

        from(source)
                .log("Input: ${body}")
                .convertBodyTo(Entity.class)
                .aggregate(constant(true), new ListAggregationStrategy())
                .completionSize(batchSize)
                .completionTimeout(batchTimeout)
                .log("Batch: ${body}")
                .process(new FilterProcessor())
                .log("Result: ${body}")
                .split()
                .body()
                .process(new DynamicRouteProcessor())
                .convertBodyTo(String.class)
                .choice()
                .when(header("IS_EXIST").isEqualTo(true))
                    .log("Filtered: ${body}")
                    .setHeader("rabbitmq.ROUTING_KEY", constant(targetQueue))
                    .to(target)
                .otherwise()
                    .log("Not exist: ${body}")
                    .setHeader("rabbitmq.ROUTING_KEY", constant(notExistQueue))
                    .to(notExist)
                .endChoice();
    }
}
