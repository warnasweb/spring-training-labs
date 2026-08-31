package com.warnasweb.training.books.observability;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {
    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("@annotation(TimedOperation)")
    public Object measure(ProceedingJoinPoint call) throws Throwable {
        long started = System.nanoTime();
        try {
            return call.proceed();
        } catch (Throwable error) {
            log.warn("{} failed with {}", call.getSignature().toShortString(), error.getClass().getSimpleName());
            throw error;
        } finally {
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            log.info("{} completed in {} ms", call.getSignature().toShortString(), elapsedMs);
        }
    }
}
