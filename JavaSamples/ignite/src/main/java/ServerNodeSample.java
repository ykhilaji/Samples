import org.apache.ignite.*;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicy;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.configuration.*;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;
import org.apache.ignite.logger.log4j.Log4JLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.jetbrains.annotations.Nullable;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.Map;

public class ServerNodeSample {
    public static void main(String[] args) throws IgniteCheckedException {
        IgniteConfiguration configuration = new IgniteConfiguration();
        configuration.setManagementThreadPoolSize(2);
        configuration.setStripedPoolSize(2);
        configuration.setSystemThreadPoolSize(2);
        configuration.setPublicThreadPoolSize(2);
        configuration.setQueryThreadPoolSize(2);
        configuration.setPeerClassLoadingThreadPoolSize(2);
        configuration.setDataStreamerThreadPoolSize(2);
        configuration.setLifecycleBeans(new LifeCycle());

        DataRegionConfiguration operational = new DataRegionConfiguration();
        operational.setName("operational");
        operational.setPersistenceEnabled(false);
        operational.setEvictionThreshold(0.75);
        operational.setPageEvictionMode(DataPageEvictionMode.RANDOM_2_LRU);
        operational.setInitialSize(10L * 1024 * 1024);
        operational.setMaxSize(50L * 1024 * 1024);

        DataRegionConfiguration storage = new DataRegionConfiguration();
        storage.setName("storage");
        storage.setPersistenceEnabled(true);
        storage.setInitialSize(50L * 1024 * 1024);
        storage.setMaxSize(100L * 1024 * 1024);

        DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
        dataStorageConfiguration.setWalMode(WALMode.DEFAULT);
        dataStorageConfiguration.setDataRegionConfigurations(operational, storage);

        CacheConfiguration<Integer, BinaryObject> operationalCacheCfg = new CacheConfiguration<>("operationalCache");
        operationalCacheCfg.setWriteThrough(true);
        operationalCacheCfg.setReadThrough(true);
        operationalCacheCfg.setWriteBehindEnabled(true);
        operationalCacheCfg.setWriteBehindBatchSize(2);
        operationalCacheCfg.setWriteBehindFlushSize(2);
        operationalCacheCfg.setWriteBehindFlushFrequency(1000);
        operationalCacheCfg.setDataRegionName("operational");
        operationalCacheCfg.setOnheapCacheEnabled(true);
        operationalCacheCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(Store.class));
        operationalCacheCfg.setStoreKeepBinary(true);

        CacheConfiguration<Integer, BinaryObject> storageCacheCfg = new CacheConfiguration<>("storageCache");
        storageCacheCfg.setDataRegionName("storage");
        storageCacheCfg.setOnheapCacheEnabled(true);
        storageCacheCfg.setEvictionPolicy(new LruEvictionPolicy(100));
        storageCacheCfg.setStoreKeepBinary(false);

        IgniteLogger logger = new Log4JLogger("D:\\Program Files\\WORK\\Samples\\JavaSamples\\ignite\\src\\main\\resources\\log4j.xml").getLogger("IGNITE_SAMPLE");
        configuration.setGridLogger(logger);

        configuration.setDataStorageConfiguration(dataStorageConfiguration);
        configuration.setCacheConfiguration(operationalCacheCfg, storageCacheCfg);
        configuration.setClientMode(false);
        configuration.setPeerClassLoadingEnabled(true);
        configuration.setDeploymentMode(DeploymentMode.SHARED);

        try(Ignite ignite = Ignition.start(configuration)) {
            ignite.active(true);
            IgniteCache<Integer, BinaryObject> operationalCache = ignite.getOrCreateCache("operationalCache").withKeepBinary();
            IgniteCache<Integer, BinaryObject> storageCache = ignite.getOrCreateCache("storageCache").withKeepBinary();

            operationalCache.put(1, ignite.binary().builder("Object").setField("field", "value").build());

            BinaryObject object = operationalCache.get(1);
            System.out.println(object.toString());

            ClusterGroup cluster = ignite.cluster().forServers();
            IgniteCompute compute = ignite.compute(cluster);
            compute.broadcast(() -> System.out.println("For servers"));

            cluster = ignite.cluster().forLocal();
            compute = ignite.compute(cluster);
            compute.broadcast(() -> System.out.println("For local"));

            cluster = ignite.cluster().forPredicate(node -> node.metrics().getTotalCpus() > 2);
            compute = ignite.compute(cluster);
            compute.broadcast(() -> System.out.println("For node with MAX CPU > 2"));
        }
    }

    public static class LifeCycle implements LifecycleBean {
        @IgniteInstanceResource
        Ignite ignite;

        @Override
        public void onLifecycleEvent(LifecycleEventType lifecycleEventType) throws IgniteException {
            if (lifecycleEventType == LifecycleEventType.BEFORE_NODE_START) {
                ignite.log().info("Before node start");
            }

            if (lifecycleEventType == LifecycleEventType.AFTER_NODE_START) {
                ignite.log().info("After node start");
            }

            if (lifecycleEventType == LifecycleEventType.BEFORE_NODE_STOP) {
                ignite.log().info("Before node stop");
            }

            if (lifecycleEventType == LifecycleEventType.AFTER_NODE_STOP) {
                ignite.log().info("After node stop");
            }
        }
    }

    public static class Store implements CacheStore<Integer, BinaryObject> {
        @Override
        public void loadCache(IgniteBiInClosure<Integer, BinaryObject> igniteBiInClosure, @Nullable Object... objects) throws CacheLoaderException {
            System.out.println("Load cache");
        }

        @Override
        public void sessionEnd(boolean b) throws CacheWriterException {
            System.out.println("Load cache");
        }

        @Override
        public BinaryObject load(Integer integer) throws CacheLoaderException {
            System.out.println("LOAD");
            return null;
        }

        @Override
        public Map<Integer, BinaryObject> loadAll(Iterable<? extends Integer> iterable) throws CacheLoaderException {
            System.out.println("LOAD ALL");
            return null;
        }

        @Override
        public void write(Cache.Entry<? extends Integer, ? extends BinaryObject> entry) throws CacheWriterException {
            System.out.println("WRITE");
        }

        @Override
        public void writeAll(Collection<Cache.Entry<? extends Integer, ? extends BinaryObject>> collection) throws CacheWriterException {
            System.out.println("WRITE ALL");
        }

        @Override
        public void delete(Object o) throws CacheWriterException {
            System.out.println("DELETE");
        }

        @Override
        public void deleteAll(Collection<?> collection) throws CacheWriterException {
            System.out.println("DELETE ALL");
        }
    }
}
