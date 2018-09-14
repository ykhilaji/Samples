package com.github.gr1f0n6x;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class MethodLoggingAspect {
    @Around("execution(public * com.github.gr1f0n6x.Service.*(..))")
    public Object aroundAllMethods(ProceedingJoinPoint point) {
        Object result = null;
        try {
            System.out.println(String.format("Before method call: %s", point.getSignature().getName()));
            result = point.proceed();
            System.out.println(String.format("After method call: %s, result: %s", point.getSignature().getName(), result.toString()));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }
}
