package com.mixc.smartconcierge.aspect;

import com.mixc.smartconcierge.annotation.AuditLog;
import com.mixc.smartconcierge.service.AuditLogWriter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogWriter auditLogWriter;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        Object result = pjp.proceed();
        auditLogWriter.write(auditLog.action(), auditLog.module());
        return result;
    }
}
