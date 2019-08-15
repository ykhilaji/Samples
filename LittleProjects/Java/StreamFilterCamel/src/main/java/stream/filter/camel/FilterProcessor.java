package stream.filter.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilterProcessor implements Processor {
    private Cache cache = new InfinispanCache();

    @Override
    public void process(Exchange exchange) throws Exception {
        List<Entity> entityList = (List<Entity>) exchange.getIn().getBody();
        List<Boolean> booleans = cache.isExist(entityList);

        Iterator<Entity> entityIterator = entityList.iterator();
        Iterator<Boolean> booleanIterator = booleans.iterator();

        List<Result> result = new ArrayList<>();

        while (entityIterator.hasNext() && booleanIterator.hasNext()) {
            result.add(new Result(booleanIterator.next(), entityIterator.next()));
        }

        exchange.getIn().setBody(result);
    }
}
