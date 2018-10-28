package com.github.gr1f0n6x.springbootcassandracrud.configuration;

import com.github.gr1f0n6x.springbootcassandracrud.filters.LoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories
@EnableCaching
public class AppConfiguration {
    @Bean
    public FilterRegistrationBean filterRegistrationBean(LoggingFilter filter) {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(filter);
        bean.addUrlPatterns("*");
        bean.setOrder(0);

        return bean;
    }
}
