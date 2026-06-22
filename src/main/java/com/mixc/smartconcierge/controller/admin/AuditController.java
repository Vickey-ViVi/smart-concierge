package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.entity.AuditLog;
import com.mixc.smartconcierge.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('管理员')")
    public ApiResult<PageResult<AuditLog>> logs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(auditService.list(username, start, end, pageNum, pageSize));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('管理员')")
    public void export(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletResponse response) throws IOException {
        List<AuditLog> logs = auditService.export(username, start, end);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=audit_logs.csv");
        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("时间,用户名,姓名,操作,模块,IP");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditLog log : logs) {
            writer.printf("%s,%s,%s,%s,%s,%s%n",
                    log.getCreateTime() != null ? log.getCreateTime().format(fmt) : "",
                    log.getUsername(), log.getRealName(), log.getAction(), log.getModule(), log.getIpAddress());
        }
        writer.flush();
    }
}
