package CassandraRepository.configuration;

import CassandraRepository.model.Entity;
import CassandraRepository.model.Key;
import CassandraRepository.repository.CassandraRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.*;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;
import org.springframework.data.cassandra.core.mapping.UserTypeResolver;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@PropertySource(value = {"classpath:cassandra.properties"})
@EnableCassandraRepositories(basePackageClasses = CassandraRepositoryImpl.class)
// EnableCassandraRepositories - requires CassandraClusterFactoryBean, CassandraMappingContext and CassandraConverter
// If you don't use cassandra repository, you can use Session and Cluster beans (the lowest api level)
// or CassandraCqlClusterFactoryBean and CassandraCqlSessionFactoryBean.
// The easier way is to extend AbstractCassandraConfiguration
public class CassandraConfiguration {
    private final Environment environment;

    @Autowired
    public CassandraConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public CassandraClusterFactoryBean cluster() {
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints(getContactPoints());
        cluster.setPort(getPort());

        cluster.setKeyspaceCreations(getKeyspaceCreations());
        cluster.setKeyspaceDrops(getKeyspaceDrops());

        return cluster;
    }

    @Bean
    public UserTypeResolver userTypeResolver() {
        return new SimpleUserTypeResolver(cluster().getObject(), getKeyspaceName());
    }

    @Bean
    public CassandraMappingContext mappingContext() {
        CassandraMappingContext mappingContext =  new CassandraMappingContext();

        Set<Class<?>> entitySet = new HashSet<>();
        entitySet.add(Entity.class);
        entitySet.add(Key.class);

        mappingContext.setInitialEntitySet(entitySet);
        mappingContext.setUserTypeResolver(userTypeResolver());

        return mappingContext;
    }

    @Bean
    public CassandraConverter converter() {
        return new MappingCassandraConverter(mappingContext());
    }

    @Bean
    public CassandraSessionFactoryBean session() throws Exception {
        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
        session.setCluster(cluster().getObject());
        session.setSchemaAction(SchemaAction.RECREATE);
        session.setConverter(converter());
        session.setKeyspaceName(getKeyspaceName());

        return session;
    }

    @Bean
    public CassandraOperations cassandraTemplate() throws Exception {
        return new CassandraTemplate(session().getObject());
    }

    private int getPort() {
        return Integer.parseInt(environment.getProperty("cassandra.port"));
    }

    private String getContactPoints() {
        return environment.getProperty("cassandra.contactpoints");
    }

    private String getKeyspaceName() {
        return environment.getProperty("cassandra.keyspace");
    }

    private List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .ifNotExists()
                .withSimpleReplication(1);

        return Collections.singletonList(specification);
    }

    private List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return Collections.singletonList(DropKeyspaceSpecification.dropKeyspace(getKeyspaceName()));
    }
}
