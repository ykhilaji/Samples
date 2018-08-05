package core.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Arrays;

public class LogInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println(String.format("Execute: %s", invocation.getMethod().getName()));
        System.out.println(String.format("Arguments: %s", Arrays.toString(invocation.getArguments())));

        return invocation.proceed();
    }
}
