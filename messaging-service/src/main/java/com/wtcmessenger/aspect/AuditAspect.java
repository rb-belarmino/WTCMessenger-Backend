package com.wtcmessenger.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Pointcut("within(com.wtcmessenger.controller..*) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void auditCreatePointcut() {}

    @AfterReturning("auditCreatePointcut()")
    public void logAuditActivity(JoinPoint joinPoint) {
        log.info("[AUDIT LOG] Operator executed modification action in CRM: {}", joinPoint.getSignature().toShortString());
    }
}
