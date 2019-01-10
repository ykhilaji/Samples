package guice.redis.crud.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import guice.redis.crud.configuration.DataSource;
import guice.redis.crud.model.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import java.util.Optional;

public class EntityRepository implements Repository<Entity, Long> {
    private static final Logger logger = LogManager.getLogger("entity-repository");

    @Inject
    DataSource<Jedis> dataSource;
    @Inject
    ObjectMapper mapper;

    @Override
    public Optional<Entity> findOne(Long aLong) throws Exception {
        return execute(client -> {
            logger.info(String.format("Get entity by id: %d", aLong));
            byte[] r = client.get(mapper.writeValueAsBytes(aLong));
            if (r != null && r.length != 0) {
                return Optional.of(mapper.readValue(r, Entity.class));
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public Entity save(Entity entity) throws Exception {
        return execute(client -> {
            logger.info(String.format("Save entity: %s", entity));
            client.set(mapper.writeValueAsBytes(entity.getId()), mapper.writeValueAsBytes(entity));
            return entity;
        });
    }

    @Override
    public Entity update(Entity entity) throws Exception {
        return execute(client -> {
            logger.info(String.format("Update entity: %s", entity));
            client.set(mapper.writeValueAsBytes(entity.getId()), mapper.writeValueAsBytes(entity));
            return entity;
        });
    }

    @Override
    public void deleteOne(Long aLong) throws Exception {
        execute(client -> {
            logger.info(String.format("Delete entity by id: %d", aLong));
            client.del(mapper.writeValueAsBytes(aLong));
            return Void.TYPE;
        });
    }

    private <T> T execute(Action<T> a) throws Exception {
        try (Jedis client = dataSource.getResource()) {
            return a.execute(client);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw e;
        }
    }

    private interface Action<T> {
        T execute(Jedis client) throws Exception;
    }
}
