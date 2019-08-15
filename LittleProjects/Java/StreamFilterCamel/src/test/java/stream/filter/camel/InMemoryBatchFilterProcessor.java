package stream.filter.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class InMemoryBatchFilterProcessor implements Processor {
    private Function<List<Entity>, List<Boolean>> predicate;

    public InMemoryBatchFilterProcessor(Function<List<Entity>, List<Boolean>> predicate) {
        this.predicate = predicate;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        List<Entity> entities = (List<Entity>) exchange.getIn().getBody();
        List<Boolean> booleans = predicate.apply(entities);

        List<Entity> result = new ArrayList<>();

        Iterator<Entity> entityIterator = entities.iterator();
        Iterator<Boolean> booleanIterator = booleans.iterator();

        while (entityIterator.hasNext() && booleanIterator.hasNext()) {
            Entity next = entityIterator.next();

            if (booleanIterator.next()) {
                result.add(next);
            }
        }

        exchange.getIn().setBody(result);
    }
}
