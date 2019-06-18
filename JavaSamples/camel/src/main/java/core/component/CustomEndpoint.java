package core.component;

import org.apache.camel.*;
import org.apache.camel.impl.DefaultEndpoint;

public class CustomEndpoint extends DefaultEndpoint {
    public CustomEndpoint(String endpointUri, CustomComponent component) {
        super(endpointUri, component);
    }

    public Producer createProducer() throws Exception {
        return new CustomProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new CustomConsumer(this, processor);
    }

    public boolean isSingleton() {
        return false;
    }
}
