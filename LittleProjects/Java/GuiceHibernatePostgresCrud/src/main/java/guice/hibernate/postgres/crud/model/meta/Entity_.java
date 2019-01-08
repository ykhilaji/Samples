package guice.hibernate.postgres.crud.model.meta;

import guice.hibernate.postgres.crud.model.Entity;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Entity.class)
public abstract class Entity_ {
    public static volatile SingularAttribute<AbstractReadWriteAccess.Item, Long> id;
    public static volatile SingularAttribute<AbstractReadWriteAccess.Item, String> value;
    public static volatile SingularAttribute<AbstractReadWriteAccess.Item, LocalDateTime> createTime;
}
