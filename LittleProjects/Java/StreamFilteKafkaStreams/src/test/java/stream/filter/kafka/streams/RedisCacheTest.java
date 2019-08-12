package stream.filter.kafka.streams;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RedisCacheTest {
    @Rule
    public GenericContainer redis = new GenericContainer("redis:5.0")
            .withExposedPorts(6379);

    private Jedis client;
    private RedisCache redisCache;

    @Before
    public void setUp() {
        client = new Jedis(redis.getContainerIpAddress(), redis.getMappedPort(6379));
        Config config = ConfigFactory.parseString(new StringBuilder()
                .append("host").append("=").append(redis.getContainerIpAddress())
                .append("\n")
                .append("port").append("=").append(redis.getMappedPort(6379))
                .toString());

        redisCache = new RedisCache(config);
    }

    @Test
    public void isExistTrue() {
        client.set("key1", "value1");
        assertTrue(redisCache.isExist(Entity.apply("key1")));
    }

    @Test
    public void isExistFalse() {
        assertFalse(redisCache.isExist(Entity.apply("key2")));
    }

    @Test
    public void isExistBatch() {
        List<Entity> entities = new ArrayList<>();
        entities.add(Entity.apply("key1"));
        entities.add(Entity.apply("key2"));
        entities.add(Entity.apply("key3"));
        entities.add(Entity.apply("key4"));
        entities.add(Entity.apply("key5"));

        client.set("key1", "value1");
        client.set("key3", "value1");
        client.set("key5", "value1");

        List<Boolean> expected = new ArrayList<>();
        expected.add(true);
        expected.add(false);
        expected.add(true);
        expected.add(false);
        expected.add(true);

        List<Boolean> result = redisCache.isExistBatch(entities);
        assertEquals(result, expected);
    }
}
