package com.github.gr1f0n6x;

import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.ssm.providers.spymemcached.MemcacheClientFactoryImpl;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import com.google.code.ssm.spring.SSMCache;
import com.google.code.ssm.spring.SSMCacheManager;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;

import java.util.Collections;

@org.springframework.context.annotation.Configuration
@EnableCaching
@ImportResource(value = "simplesm-context.xml")
public class Configuration {

    @Bean
    public FilterRegistrationBean filterRegistrationBean(RequestLoggingFilter requestLoggingFilter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();

        filterRegistrationBean.setOrder(0);
        filterRegistrationBean.setName("request logger");
        filterRegistrationBean.setFilter(requestLoggingFilter);
        filterRegistrationBean.addUrlPatterns("*");

        return filterRegistrationBean;
    }

    @Bean
    public CacheManager cacheManager() {
        SSMCacheManager cacheManager = new SSMCacheManager();

        SSMCache cache = new SSMCache(defaultCache().getCache(), 300, true);
        cacheManager.setCaches(Collections.singletonList(cache));

        return cacheManager;
    }

    @Bean
    @DependsOn("cacheBase")
    public CacheFactory defaultCache() {
        CacheFactory cacheFactory = new CacheFactory();

        CacheConfiguration cacheConfiguration = new CacheConfiguration();

        cacheConfiguration.setConsistentHashing(false);
        cacheConfiguration.setUseBinaryProtocol(true);
        cacheConfiguration.setOperationTimeout(10);

        cacheFactory.setCacheName("entities");
        cacheFactory.setAddressProvider(new DefaultAddressProvider("192.168.99.100:11211"));
        cacheFactory.setCacheClientFactory(new MemcacheClientFactoryImpl());
        cacheFactory.setConfiguration(cacheConfiguration);

        return cacheFactory;
    }
}