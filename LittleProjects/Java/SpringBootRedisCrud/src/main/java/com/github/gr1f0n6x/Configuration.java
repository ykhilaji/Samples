package com.github.gr1f0n6x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    @Autowired
    public FilterRegistrationBean filterRegistrationBean(LoggingFilter filter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setName("logger");
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("*");

        return filterRegistrationBean;
    }
}
