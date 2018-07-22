package annotation;

import com.google.inject.AbstractModule;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Service.class).to(ServiceImpl.class);
        bind(Source.class).annotatedWith(MySQL.class).to(MySQLSource.class);
        bind(Source.class).annotatedWith(PostgreSQL.class).to(PostgreSQLSource.class);
    }
}
