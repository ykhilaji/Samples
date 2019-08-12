package stream.filter.kafka.streams;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RedisCache implements Cache {
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);
    private JedisPool pool;

    public RedisCache(Config config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        this.pool = new JedisPool(poolConfig, config.getString("host"), config.getInt("port"));
    }

    @Override
    public boolean isExist(Entity entity) {
        try (Jedis client = pool.getResource()) {
            logger.info("Check if entity exists: {}", entity);
            return client.exists(entity.getKey());
        }
    }

    @Override
    public List<Boolean> isExistBatch(List<Entity> entities) {
        try (Jedis client = pool.getResource()) {
            logger.info("Check if entities exists: {}", entities);
            List<Response<Boolean>> responses = new ArrayList<>();

            Pipeline pipeline = client.pipelined();

            entities.forEach(entity -> responses.add(pipeline.exists(entity.getKey())));
            pipeline.sync();

            return responses
                    .stream()
                    .map(Response::get)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void close() {
        pool.close();
    }
}
