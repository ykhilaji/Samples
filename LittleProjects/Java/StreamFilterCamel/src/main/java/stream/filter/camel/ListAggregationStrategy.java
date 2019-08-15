package stream.filter.camel;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;

public class ListAggregationStrategy extends AbstractListAggregationStrategy<Entity> {
    @Override
    public Entity getValue(Exchange exchange) {
        return exchange.getIn().getBody(Entity.class);
    }
}
