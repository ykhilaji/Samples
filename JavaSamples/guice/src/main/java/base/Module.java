package base;

import com.google.inject.AbstractModule;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Service.class).to(ServiceImpl.class);
        bind(Repository.class).to(RepositoryImpl.class);
        bind(DataSource.class).to(DataSourceImpl.class);
    }
}
