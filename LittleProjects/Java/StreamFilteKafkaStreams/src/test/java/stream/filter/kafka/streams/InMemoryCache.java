package stream.filter.kafka.streams;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryCache implements Cache {
    private Function<Entity, Boolean> predicate;

    public InMemoryCache(Function<Entity, Boolean> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean isExist(Entity entity) {
        return predicate.apply(entity);
    }

    @Override
    public List<Boolean> isExistBatch(List<Entity> entities) {
        return entities.stream().map(e -> predicate.apply(e)).collect(Collectors.toList());
    }

    @Override
    public void close() {

    }
}
