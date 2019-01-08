package guice.hibernate.postgres.crud.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class LogInterceptor implements MethodInterceptor {
    private Logger logger = LogManager.getLogger("log-interceptor");

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        logger.info(String.format("Execute: %s", invocation.getMethod().getName()));
        logger.info(String.format("Arguments: %s", Arrays.toString(invocation.getArguments())));

        try {
            Object result = invocation.proceed();
            logger.info(String.format("Result: %s", result));

            return result;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw e;
        }
    }
}
