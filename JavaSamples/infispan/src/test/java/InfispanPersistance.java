import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.jdbc.DatabaseType;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.util.concurrent.IsolationLevel;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import static org.junit.Assert.*;


public class InfispanPersistance {
    private static Cache<Object, Object> cache;
    private static EmbeddedCacheManager manager;

    @BeforeClass
    public static void initCache() throws IOException {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder
                .clustering()
                .cacheMode(CacheMode.LOCAL)
                .remoteTimeout(10, TimeUnit.SECONDS)
                .memory()
                .evictionType(EvictionType.COUNT)
                .storageType(StorageType.OBJECT)
                .size(10)
                .locking()
                .isolationLevel(IsolationLevel.READ_COMMITTED)
                .concurrencyLevel(2)
                .transaction()
                .invocationBatching()
                .enable(true);

        configurationBuilder
                .persistence()
                .passivation(false)
                    .addStore(JdbcStringBasedStoreConfigurationBuilder.class)
                    .fetchPersistentState(false)
                    .ignoreModifications(false)
                    .purgeOnStartup(true)
                    .shared(false)
                    .dialect(DatabaseType.POSTGRES)
                    .table()
                        .dropOnExit(false)
                        .createOnStart(true)
                        .tableNamePrefix("persistance_store")
                        .idColumnName("ID_COLUMN").idColumnType("VARCHAR(255)")
                        .dataColumnName("DATA_COLUMN").dataColumnType("bytea")
                        .timestampColumnName("TIMESTAMP_COLUMN").timestampColumnType("BIGINT")
                    .connectionPool()
                        .connectionUrl("jdbc:postgresql://192.168.99.100:5432/postgres")
                        .username("postgres")
                        .driverClass("org.postgresql.Driver");

        Configuration configuration = configurationBuilder.build();

        manager = new DefaultCacheManager(configuration);
        manager.start();

        System.out.println(String.format("Cluster name: %s", manager.getClusterName()));
        System.out.println(String.format("Number of CPUs: %d", manager.getHealth().getHostInfo().getNumberOfCpus()));
        System.out.println(String.format("Total memory in kb: %d", manager.getHealth().getHostInfo().getTotalMemoryKb()));

        cache = manager.getCache();
    }

    @AfterClass
    public static void destroyCache() {
        System.out.println(String.format("Average read time: %d", manager.getStats().getAverageReadTime()));
        System.out.println(String.format("Average remove time: %d", manager.getStats().getAverageRemoveTime()));
        System.out.println(String.format("Average write time: %d", manager.getStats().getAverageWriteTime()));
        System.out.println(String.format("Off heap-memory used: %d", manager.getStats().getOffHeapMemoryUsed()));

        cache.stop();
        manager.stop();
    }

    @Before
    public void clearCache() {
        cache.clear();
    }

    @After
    public void showStats() {
        System.out.println(String.format("Free memory in kb: %d", manager.getHealth().getHostInfo().getFreeMemoryInKb()));
    }
}

