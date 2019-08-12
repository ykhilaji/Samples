package stream.filter.kafka.streams;

import com.typesafe.config.Config;

import java.util.function.Function;

public class InMemoryCacheBuilder implements CacheBuilder {
    private Function<Entity, Boolean> predicate;

    public InMemoryCacheBuilder(Function<Entity, Boolean> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Cache build(Config config) {
        return new InMemoryCache(predicate);
    }
}
