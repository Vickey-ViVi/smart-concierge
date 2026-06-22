package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.entity.AuditLog;
import com.mixc.smartconcierge.repository.AuditLogRepository;
import com.mixc.smartconcierge.security.AdminUserDetailsService;
import com.mixc.smartconcierge.util.RequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogWriter {

    private final AuditLogRepository auditLogRepository;
    private final AdminUserDetailsService adminUserDetailsService;

    @Async
    public void write(String action, String module) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return;
            }
            String username = auth.getName();
            var user = adminUserDetailsService.loadEntity(username);
            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setRealName(user.getRealName());
            log.setAction(action);
            log.setModule(module);
            log.setIpAddress(RequestUtil.getClientIp());
            log.setUserAgent(RequestUtil.getUserAgent());
            auditLogRepository.save(log);
        } catch (Exception ignored) {
        }
    }
}
