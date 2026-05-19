package com.wtcmessenger.aspect;

import com.wtcmessenger.model.AuditLog;
import com.wtcmessenger.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Intercepta todos os POST, PUT, DELETE em Controllers para fins de auditoria detalhada de modificações
    @Pointcut("within(com.wtcmessenger.controller..*) && (" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void auditModifyPointcut() {}

    @AfterReturning("auditModifyPointcut()")
    public void logAuditActivity(JoinPoint joinPoint) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null) ? auth.getName() : "Anonymous/System";
            String roles = (auth != null) ? auth.getAuthorities().toString() : "[]";
            String action = joinPoint.getSignature().toShortString();
            String payload = Arrays.toString(joinPoint.getArgs());

            // Cria e salva o log de auditoria no MongoDB em tempo real
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .roles(roles)
                    .action(action)
                    .payload(payload)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            // Log de Console estruturado de alto nível
            log.info("[AUDIT LOG SUCCESS] User: '{}' with roles {} performed operation: {} | Payload: {}",
                    username, roles, action, payload);
        } catch (Exception e) {
            log.error("[AUDIT LOG ERROR] Failed to record audit log: {}", e.getMessage());
        }
    }
}
