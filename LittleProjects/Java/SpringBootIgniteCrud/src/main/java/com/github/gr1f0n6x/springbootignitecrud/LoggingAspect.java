package com.github.gr1f0n6x.springbootignitecrud;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggingAspect {
    @Around("execution(public * com.github.gr1f0n6x.springbootignitecrud.EntityService.*(..))")
    public Object around(ProceedingJoinPoint point) {
        Object result = null;
        System.out.println(String.format("EXECUTE: %s", point.getSignature().getName()));
        try {
            result = point.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        System.out.println(String.format("RESULT OF %s: %s", point.getSignature().getName(), result != null ? result.toString() : null));
        return result;
    }
}
