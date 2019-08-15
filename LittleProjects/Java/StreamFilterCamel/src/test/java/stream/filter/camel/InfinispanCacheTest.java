package stream.filter.camel;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InfinispanCacheTest {
    private static InfinispanCache cache = new InfinispanCache();

    @AfterEach
    public void clear() {
        cache.getCache().clear();
    }

    @AfterClass
    public static void stop() {
        cache.getCache().stop();
    }

    @Test
    public void isExistTrue() {
        cache.getCache().put("1", true);
        assertTrue(cache.isExist(new Entity("1")));
    }

    @Test
    public void isExistFalse() {
        assertFalse(cache.isExist(new Entity("2")));
    }

    @Test
    public void isExistBatch() {
        List<Entity> entities = new ArrayList<>();
        entities.add(new Entity("1")); cache.getCache().put("1", true);
        entities.add(new Entity("2"));
        entities.add(new Entity("3")); cache.getCache().put("3", true);

        List<Boolean> booleans = cache.isExist(entities);
        List<Boolean> expected = new ArrayList<>();
        expected.add(true);
        expected.add(false);
        expected.add(true);

        assertEquals(expected, booleans);

    }
}
