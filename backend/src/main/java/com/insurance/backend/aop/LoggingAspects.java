package com.insurance.backend.aop;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
@Component
@Aspect
public class LoggingAspects {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspects.class);
    @Before("execution(* com.insurance.backend.controller.*.*(..))")
    public void logMethodCall() {
        logger.info("Method execution started.");
    }

}