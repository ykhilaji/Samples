package aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class AopLogic implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Before method call");
        Object result = invocation.proceed();
        System.out.println("After method call");

        return result;
    }
}
