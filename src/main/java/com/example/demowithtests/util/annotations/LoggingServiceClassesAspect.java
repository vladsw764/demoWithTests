package com.example.demowithtests.util.annotations;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import static com.example.demowithtests.util.annotations.LogColorConstants.*;

@Log4j2
@Aspect
@Component
public class LoggingServiceClassesAspect {

    private final Statistics hibernateStatistics;

    private LocalDateTime startTime;

    public LoggingServiceClassesAspect(EntityManagerFactory entityManagerFactory) {
        this.hibernateStatistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Pointcut("execution(public * com.example.demowithtests.service..*.*(..))")
    public void callAtMyServicesPublicMethods() {
    }

    @AfterReturning("execution(* com.example.demowithtests.service.fillDataBase.LoaderServiceBean.*(..))")
    public void countSqlQueries() {
        long queryCount = hibernateStatistics.getPrepareStatementCount();

        log.debug(ANSI_CYAN + "Total SQL queries executed: {}" + ANSI_RESET, queryCount);
    }

    @Before("callAtMyServicesPublicMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            log.debug(ANSI_BLUE + "Service: " + methodName + " - start. Args count - {}" + ANSI_RESET, args.length);
        } else {
            log.debug(ANSI_BLUE + "Service: " + methodName + " - start." + ANSI_RESET);
        }
        startTime = LocalDateTime.now();
        log.debug(ANSI_BLUE + "{} started at {}" + ANSI_RESET, methodName, startTime);
    }

    @AfterReturning(value = "callAtMyServicesPublicMethods()", returning = "returningValue")
    public void logAfter(JoinPoint joinPoint, Object returningValue) {
        String methodName = joinPoint.getSignature().toShortString();
        Object outputValue;
        if (returningValue != null) {
            if (returningValue instanceof Collection) {
                outputValue = "Collection size - " + ((Collection<?>) returningValue).size();
            } else if (returningValue instanceof byte[]) {
                outputValue = "File as byte[]";
            } else {
                outputValue = returningValue;
            }
            log.debug(ANSI_BLUE + "Service: " + methodName + " - end. Returns - {}" + ANSI_RESET, outputValue);
        } else {
            log.debug(ANSI_BLUE + "Service: " + methodName + " - end." + ANSI_RESET);
        }

        String input = Arrays.toString(joinPoint.getArgs());
        LocalDateTime endTime = LocalDateTime.now();
        log.debug(ANSI_BLUE + "{} with input {} completed in {} milliseconds" + ANSI_RESET, methodName, input,
                Duration.between(startTime, endTime).toMillis());
    }
}
