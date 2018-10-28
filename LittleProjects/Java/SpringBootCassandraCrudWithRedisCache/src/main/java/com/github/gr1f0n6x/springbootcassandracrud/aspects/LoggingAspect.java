package com.github.gr1f0n6x.springbootcassandracrud.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggingAspect {
    @Around("execution(public * com.github.gr1f0n6x.springbootcassandracrud.service.*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        System.out.println(String.format("Before: %s", point.getSignature().getName()));
        Object result = point.proceed();
        System.out.println(String.format("After: %s, result: %s", point.getSignature().getName(), result.toString()));
        return result;
    }
}
