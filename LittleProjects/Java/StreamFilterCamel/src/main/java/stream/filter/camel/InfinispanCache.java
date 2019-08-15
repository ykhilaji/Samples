package stream.filter.camel;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InfinispanCache implements Cache {
    private EmbeddedCacheManager cacheManager;
    private org.infinispan.Cache<String, Boolean> cache;

    public InfinispanCache() {
        cacheManager = new DefaultCacheManager();

        Configuration configuration = new ConfigurationBuilder()
                .clustering()
                .cacheMode(CacheMode.LOCAL)
                .build();
        cacheManager.defineConfiguration("entity", configuration);

        cache = cacheManager.getCache("entity");

        // initial cache values
        IntStream.range(0, 10).forEach(i -> cache.put(String.valueOf(i), true));
    }

    public org.infinispan.Cache<String, Boolean> getCache() {
        return cache;
    }

    @Override
    public boolean isExist(Entity entity) {
        return cache.containsKey(entity.getKey());
    }

    @Override
    public List<Boolean> isExist(List<Entity> entities) {
        return entities.stream().map(this::isExist).collect(Collectors.toList());
    }
}
