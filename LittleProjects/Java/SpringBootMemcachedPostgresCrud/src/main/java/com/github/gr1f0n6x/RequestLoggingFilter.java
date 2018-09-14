package com.github.gr1f0n6x;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

@Component
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {
    @Override
    protected void beforeRequest(HttpServletRequest httpServletRequest, String s) {
        System.out.println(String.format("Before request: %s", httpServletRequest.getRequestURI()));
    }

    @Override
    protected void afterRequest(HttpServletRequest httpServletRequest, String s) {
        System.out.println(String.format("After request: %s", httpServletRequest.getRequestURI()));
    }
}
