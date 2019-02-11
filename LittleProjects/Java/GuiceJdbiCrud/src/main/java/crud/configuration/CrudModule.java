package crud.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.zaxxer.hikari.HikariDataSource;
import crud.model.Entity;
import crud.repository.EntityRepository;
import crud.repository.Repository;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Singleton;
import javax.sql.DataSource;

public class CrudModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<Repository<Entity, Long>>(){}).to(new TypeLiteral<EntityRepository>(){}).in(Singleton.class);
    }

    @Provides
    @Singleton
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://192.168.99.100:5432/postgres");
        ds.setUsername("postgres");
        ds.setPassword("");
        ds.setSchema("public");
        ds.setMaximumPoolSize(4);
        ds.setMinimumIdle(2);

        return ds;
    }

    @Provides
    @Singleton
    @Inject
    public Jdbi jdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.registerRowMapper(Entity.class, new Entity.EntityMapper());

        return jdbi;
    }
}
