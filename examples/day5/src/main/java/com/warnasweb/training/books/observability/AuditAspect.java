package com.warnasweb.training.books.observability;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @AfterReturning(pointcut = "@annotation(audited)")
    public void succeeded(JoinPoint call, Audited audited) {
        log.info("audit action={} outcome=SUCCESS method={}", audited.value(), call.getSignature().toShortString());
    }

    @AfterThrowing(pointcut = "@annotation(audited)", throwing = "error")
    public void failed(JoinPoint call, Audited audited, Throwable error) {
        log.info("audit action={} outcome=FAILURE method={} error={}", audited.value(),
                call.getSignature().toShortString(), error.getClass().getSimpleName());
    }
}
