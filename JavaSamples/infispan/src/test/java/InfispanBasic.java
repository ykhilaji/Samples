import org.infinispan.Cache;
import org.infinispan.filter.KeyFilter;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.*;
import org.infinispan.notifications.cachelistener.event.*;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class InfispanBasic {
    private static Cache<Object, Object> cache;
    private static EmbeddedCacheManager manager;

    @BeforeClass
    public static void initCache() throws IOException {
        manager = new DefaultCacheManager("infispanbasic.xml");
        manager.start();

        System.out.println(String.format("Cluster name: %s", manager.getClusterName()));
        System.out.println(String.format("Physical addresses: %s", manager.getTransport().getPhysicalAddresses()));
        System.out.println(String.format("Number of CPUs: %d", manager.getHealth().getHostInfo().getNumberOfCpus()));
        System.out.println(String.format("Total memory in kb: %d", manager.getHealth().getHostInfo().getTotalMemoryKb()));


        cache = manager.getCache();

        cache.addListener(new CacheEventListener(), new CustomKeyFilter("key"));
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

    @Test
    public void cachePut() {
        cache.put("key", "value");

        assertEquals(1, cache.size());
        assertEquals("value", cache.get("key"));
    }

    @Test
    public void cacheExpire() throws InterruptedException {
        cache.put("expire", "value", 1, TimeUnit.SECONDS);

        assertEquals("value", cache.get("expire"));
        Thread.sleep(1500);
        assertNull(cache.get("expire"));
    }

    @Test
    public void cachePutAsync() {
        cache.putAsync("asyncKey", "asyncObject").thenAccept(o -> {
            try {
                assertEquals("asyncObject", cache.getAsync("asyncKey").get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void forEachCompute() {
        for (int i = 0; i < 20; ++i) {
            cache.putForExternalRead(i, i);
        }

        List<Integer> integers = new ArrayList<>();
        cache.forEach((key, value) -> integers.add((Integer) value));

        assertEquals(190, integers.stream().reduce(0, (a, b) -> a + b).longValue());
    }

    @Test
    public void cacheReplace() throws InterruptedException {
        cache.put("forReplace", "initialValue");
        assertEquals("initialValue", cache.get("forReplace"));

        cache.replace("forReplace", "replacedValue");
        assertEquals("replacedValue", cache.get("forReplace"));

        assertFalse(cache.replace("forReplace", "incorrectValue", "newValue"));

        cache.replace("forReplace", "newValue", 1, TimeUnit.SECONDS);
        assertEquals("newValue", cache.get("forReplace"));

        Thread.sleep(1500);
        assertNull(cache.get("forReplace"));
    }

    @Test
    public void isCacheRunning() {
        assertTrue(manager.isRunning("local"));
        assertFalse(manager.isRunning("anotherCache"));
    }

    @Listener(clustered = false)
    public static class CacheEventListener {

        @CacheEntryCreated
        public void entryCreated(CacheEntryCreatedEvent event) {
            System.out.println("New entry " + event.getKey() + " created in the cache");
        }


        @CacheEntryModified
        public void entryModified(CacheEntryModifiedEvent<String, String> event) {
            if (event.isPre())
                return;

            System.out.println(String.format("Cache entry %s = %s modified in cache %s", event.getKey(), event.getValue(), event.getCache()));
        }

        @CacheEntryRemoved
        public void entryRemoved(CacheEntryRemovedEvent<String, String> event) {
            if (event.isPre())
                return;
            System.out.println(String.format("Cache entry %s removed in cache %s", event.getKey(), event.getCache()));
        }

        @CacheEntriesEvicted
        public void entryEvicted(CacheEntriesEvictedEvent<String, String> event) {
            System.out.println("Evicted");
        }

        @CacheEntryExpired
        public void entryExpired(CacheEntryExpiredEvent<String, String> event) {
            System.out.println("Expired");
        }
    }

    public static class CustomKeyFilter implements KeyFilter<Object> {
        private final Object keyToAccept;

        public CustomKeyFilter(String keyToAccept) {
            if (keyToAccept == null) {
                throw new NullPointerException();
            }
            this.keyToAccept = keyToAccept;
        }

        public boolean accept(Object key) {
            return keyToAccept.equals(key);
        }
    }
}
