package guice.redis.crud.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import guice.redis.crud.model.Entity;
import guice.redis.crud.repository.EntityRepository;
import guice.redis.crud.repository.Repository;
import redis.clients.jedis.Jedis;

public class GuiceRedisCrudModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<Repository<Entity, Long>>(){}).to(new TypeLiteral<EntityRepository>(){}).in(Singleton.class);
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    public DataSource<Jedis> dataSource() {
        return new RedisDataSource("192.168.99.100");
    }
}
