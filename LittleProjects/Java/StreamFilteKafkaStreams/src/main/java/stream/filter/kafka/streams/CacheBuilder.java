package stream.filter.kafka.streams;

import com.typesafe.config.Config;

public interface CacheBuilder {
    Cache build(Config config);
}
