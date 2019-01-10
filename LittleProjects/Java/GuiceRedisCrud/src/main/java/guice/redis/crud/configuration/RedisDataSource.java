package guice.redis.crud.configuration;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDataSource implements DataSource<Jedis> {
    private JedisPool pool;

    public RedisDataSource() {
        this("localhost");
    }

    public RedisDataSource(String host) {
        this(host, 6379);
    }

    public RedisDataSource(String host, int port) {
        this(8, host, port);
    }

    public RedisDataSource(int connections, String host, int port) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(connections);

        this.pool = new JedisPool(config, host, port);
    }

    public Jedis getResource() {
        return this.pool.getResource();
    }
}
