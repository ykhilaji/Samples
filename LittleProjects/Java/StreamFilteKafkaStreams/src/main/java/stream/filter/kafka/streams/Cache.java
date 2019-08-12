package stream.filter.kafka.streams;

import java.util.List;

public interface Cache {
    boolean isExist(Entity entity);

    List<Boolean> isExistBatch(List<Entity> entities);

    void close();
}
