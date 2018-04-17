package core.aspect;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    private static Logger logger = Logger.getLogger(LoggingAspect.class.getSimpleName());

    @Before("execution(* core.controller.*.*(..))")
    public void log(JoinPoint joinPoint) throws Throwable {
        logger.info(String.format("Call: %s", joinPoint.getSignature().getName()));
        logger.info(String.format("Arguments: %s", Arrays.toString(joinPoint.getArgs())));
    }
}
