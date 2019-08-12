package stream.filter.kafka.streams;

import com.typesafe.config.Config;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;

public class FilterLogicSupplier implements TransformerSupplier<String, Entity, KeyValue<String, Entity>> {
    private Config config;
    private CacheBuilder builder;

    public FilterLogicSupplier(CacheBuilder builder, Config config) {
        this.config = config;
        this.builder = builder;
    }

    @Override
    public Transformer<String, Entity, KeyValue<String, Entity>> get() {
        return new FilterLogic(builder, config);
    }

    private static class FilterLogic implements Transformer<String, Entity, KeyValue<String, Entity>> {
        private Cache cache;
        private ProcessorContext context;
        private Config config;
        private CacheBuilder builder;

        public FilterLogic(CacheBuilder builder, Config config) {
            this.config = config;
            this.builder = builder;
        }

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.cache = builder.build(config);
        }

        @Override
        public KeyValue<String, Entity> transform(String key, Entity value) {
            if (cache.isExist(value)) {
                context.forward(key, value);
            }

            return null;
        }

        @Override
        public void close() {
            cache.close();
        }
    }
}
