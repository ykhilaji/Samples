import org.apache.ignite.*;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.configuration.*;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;
import org.apache.ignite.logger.log4j.Log4JLogger;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.stream.StreamVisitor;
import org.jetbrains.annotations.Nullable;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.Map;

public class StreamingSample {
    public static void main(String[] args) throws IgniteCheckedException {
        IgniteConfiguration configuration = new IgniteConfiguration();
        configuration.setLifecycleBeans(new LifeCycle());

        CacheConfiguration<Integer, Integer> firstCfg = new CacheConfiguration<>("first");
        firstCfg.setWriteThrough(true);
        firstCfg.setReadThrough(true);
        firstCfg.setWriteBehindEnabled(true);
        firstCfg.setWriteBehindBatchSize(2);
        firstCfg.setWriteBehindFlushSize(2);
        firstCfg.setWriteBehindFlushFrequency(1000);
        firstCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(Store.class));

        CacheConfiguration<Integer, Integer> secondCfg = new CacheConfiguration<>("second");

        IgniteLogger logger = new Log4JLogger("D:\\Program Files\\WORK\\Samples\\JavaSamples\\ignite\\src\\main\\resources\\log4j.xml").getLogger("IGNITE_SAMPLE");
        configuration.setGridLogger(logger);

        configuration.setCacheConfiguration(firstCfg, secondCfg);

        try(Ignite ignite = Ignition.start(configuration)) {
            ignite.active(true);
            IgniteCache<Integer, Integer> first = ignite.getOrCreateCache("first").withKeepBinary();
            IgniteCache<Integer, Integer> second = ignite.getOrCreateCache("second").withKeepBinary();

            try(IgniteDataStreamer<Integer, Integer> streamer = ignite.dataStreamer("first")) {
                streamer.allowOverwrite(true); // if false (default) - will skip CacheStore

                for(int i = 0; i < 20; ++i) {
                    streamer.addData(i, i);
                }
            }

            ignite.log().info(String.format("First cache size: %d", first.size()));
            ignite.log().info(String.format("Second cache size: %d", second.size()));

            first.clear();
            second.clear();

            try(IgniteDataStreamer<Integer, Integer> streamer = ignite.dataStreamer("first")) {
                streamer.allowOverwrite(true);

                streamer.receiver(StreamVisitor.from((cache, entry) -> {
                    // write data only into second cache
                    second.put(entry.getKey(), entry.getValue());
                }));

                for(int i = 0; i < 20; ++i) {
                    streamer.addData(i, i);
                }
            }

            ignite.log().info(String.format("First cache size: %d", first.size()));
            ignite.log().info(String.format("Second cache size: %d", second.size()));

            first.clear();
            second.clear();
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

    public static class Store implements CacheStore<Integer, Integer> {
        @Override
        public void loadCache(IgniteBiInClosure<Integer, Integer> igniteBiInClosure, @Nullable Object... objects) throws CacheLoaderException {
            System.out.println("Load cache");
        }

        @Override
        public void sessionEnd(boolean b) throws CacheWriterException {
            System.out.println("Load cache");
        }

        @Override
        public Integer load(Integer integer) throws CacheLoaderException {
            System.out.println("LOAD");
            return null;
        }

        @Override
        public Map<Integer, Integer> loadAll(Iterable<? extends Integer> iterable) throws CacheLoaderException {
            System.out.println("LOAD ALL");
            return null;
        }

        @Override
        public void write(Cache.Entry<? extends Integer, ? extends Integer> entry) throws CacheWriterException {
            System.out.println("WRITE");
        }

        @Override
        public void writeAll(Collection<Cache.Entry<? extends Integer, ? extends Integer>> collection) throws CacheWriterException {
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

