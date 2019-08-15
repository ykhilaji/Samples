package stream.filter.camel;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class StreamFilter {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load().getConfig("filter");
        CamelContext camelContext = new DefaultCamelContext();

        camelContext.getTypeConverterRegistry().addTypeConverters(new EntityConverter());
        camelContext.addRoutes(new FilterRoute(config));

        camelContext.start();
    }
}
