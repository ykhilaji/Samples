package stream.filter.kafka.streams;

import com.typesafe.config.Config;

public class RedisCacheBuilder implements CacheBuilder {
    @Override
    public Cache build(Config config) {
        return new RedisCache(config);
    }
}
