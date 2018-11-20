package com.github.gr1f0n6x.springbootignitecrud;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;
import org.apache.ignite.cache.store.jdbc.JdbcType;
import org.apache.ignite.cache.store.jdbc.JdbcTypeField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableIgniteRepositories(basePackageClasses = EntityRepository.class)
public class AppConfiguration {
    @Bean
    public FilterRegistrationBean filterRegistrationBean(LoggingFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setOrder(0);
        registrationBean.setFilter(filter);
        registrationBean.setName("logging filter");
        registrationBean.addUrlPatterns("*");

        return registrationBean;
    }

    @Bean
    public Ignite igniteInstance(DataSource dataSource) {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName("igniteCrud");
        cfg.setPeerClassLoadingEnabled(true);

        CacheConfiguration<Long, Entity> cacheCfg = new CacheConfiguration<>("entity");
        cacheCfg.setIndexedTypes(Long.class, Entity.class);
        cacheCfg.setWriteBehindEnabled(true);
        cacheCfg.setWriteThrough(true);
        cacheCfg.setReadThrough(true);
        CacheJdbcPojoStoreFactory<Long, Entity> factory = new CacheJdbcPojoStoreFactory<>();
        factory.setDataSource(dataSource);
        JdbcType jdbcType = new JdbcType();
        jdbcType.setCacheName("entity");
        jdbcType.setKeyType(Long.class);
        jdbcType.setValueType(Entity.class);
        jdbcType.setDatabaseTable("entity");
        jdbcType.setDatabaseSchema("public");
        jdbcType.setKeyFields(new JdbcTypeField(Types.NUMERIC, "id", Long.class, "id"));
        jdbcType.setValueFields(new JdbcTypeField(Types.NUMERIC, "id", Long.class, "id"), new JdbcTypeField(Types.VARCHAR, "value", String.class, "value"));
        factory.setTypes(jdbcType);
        cacheCfg.setCacheStoreFactory(factory);

        cfg.setCacheConfiguration(cacheCfg);

        return Ignition.start(cfg);
    }

    @Bean
    public HealthIndicator healthIndicator() {
        return () -> Health.up().withDetail("OK", "Some info").build();
    }

    @Bean
    public Endpoint<List<String>> endpoint() {
        return new Endpoint<List<String>>() {
            @Override
            public String getId() {
                return "myEndpoint";
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isSensitive() {
                return true;
            }

            @Override
            public List<String> invoke() {
                return Collections.singletonList("Some info");
            }
        };
    }
}
