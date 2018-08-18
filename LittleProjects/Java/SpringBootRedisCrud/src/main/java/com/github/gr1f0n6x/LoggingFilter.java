package com.github.gr1f0n6x;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoggingFilter extends AbstractRequestLoggingFilter {
    @Override
    protected void beforeRequest(HttpServletRequest httpServletRequest, String s) {
        System.out.println(String.format("Request URI: %s", httpServletRequest.getRequestURI()));
        System.out.println(String.format("Request URL: %s", httpServletRequest.getRequestURL().toString()));
        System.out.println(String.format("Request method: %s", httpServletRequest.getMethod()));
    }

    @Override
    protected void afterRequest(HttpServletRequest httpServletRequest, String s) {
        System.out.println(String.format("Request URI: %s processed", httpServletRequest.getRequestURI()));
    }
}
