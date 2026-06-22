package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.entity.AuditLog;
import com.mixc.smartconcierge.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public PageResult<AuditLog> list(String username, LocalDateTime start, LocalDateTime end,
                                     int pageNum, int pageSize) {
        Page<AuditLog> page = auditLogRepository.search(username, start, end,
                PageRequest.of(Math.max(pageNum - 1, 0), pageSize));
        return new PageResult<>(page.getTotalElements(), page.getContent());
    }

    public List<AuditLog> export(String username, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.search(username, start, end, PageRequest.of(0, 10000)).getContent();
    }
}
