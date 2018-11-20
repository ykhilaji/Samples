package com.github.gr1f0n6x.springbootignitecrud;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoggingFilter extends AbstractRequestLoggingFilter {
    @Override
    protected void beforeRequest(HttpServletRequest httpServletRequest, String s) {
        System.out.println(String.format("BEFORE: %s", httpServletRequest.getRequestURI()));
    }

    @Override
    protected void afterRequest(HttpServletRequest httpServletRequest, String s) {
        System.out.println(String.format("AFTER: %s", httpServletRequest.getRequestURI()));
    }
}
