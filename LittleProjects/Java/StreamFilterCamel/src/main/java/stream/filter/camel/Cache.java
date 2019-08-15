package stream.filter.camel;

import java.util.List;

public interface Cache {
    boolean isExist(Entity entity);

    List<Boolean> isExist(List<Entity> entities);
}
