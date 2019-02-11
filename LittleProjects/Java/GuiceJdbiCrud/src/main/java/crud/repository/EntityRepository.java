package crud.repository;

import com.google.inject.Inject;
import crud.model.Entity;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class EntityRepository implements Repository<Entity, Long> {
    @Inject
    private Jdbi jdbi;

    @Override
    public Entity save(Entity entity) {
        try {
            return jdbi.withHandle(handle -> handle.inTransaction(h ->
                    h.createUpdate("insert into entity(value) values (:value)")
                            .bind("value", entity.getValue())
                            .executeAndReturnGeneratedKeys()
                            .mapTo(Entity.class)
                            .findOnly()
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteById(Long aLong) {
        try {
            return jdbi.withHandle(handle -> handle.inTransaction(h ->
                    h.createUpdate("delete from entity where id = :id")
                            .bind("id", aLong)
                            .execute() != 0
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Entity update(Entity entity) {
        try {
            return jdbi.withHandle(handle -> handle.inTransaction(h -> {
                        h.createUpdate("update entity set value = :value where id = :id")
                                .bind("value", entity.getValue())
                                .bind("id", entity.getId())
                                .execute();

                        return entity;
                    }
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Entity> findOne(Long aLong) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select id, value from entity where id = :id")
                        .bind("id", aLong)
                        .mapTo(Entity.class)
                        .findFirst());
    }

    @Override
    public Iterable<Entity> findAll() {
        return jdbi.withHandle(handle ->
                handle.createQuery("select id, value from entity")
                        .mapTo(Entity.class)
                        .list());
    }
}
