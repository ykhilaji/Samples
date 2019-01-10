package guice.redis.crud.configuration;

import java.io.Closeable;

public interface DataSource<T extends Closeable> {
    T getResource();
}
