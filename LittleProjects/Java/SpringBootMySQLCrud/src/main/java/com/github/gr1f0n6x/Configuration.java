package com.github.gr1f0n6x;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    public FilterRegistrationBean filterRegistrationBean(LoggingFilter loggingFilter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(loggingFilter);
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("*");
        filterRegistrationBean.setName("loggingFilter");

        return filterRegistrationBean;
    }
}
