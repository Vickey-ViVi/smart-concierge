package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.dto.ComplaintHandleRequest;
import com.mixc.smartconcierge.service.AuditLogWriter;
import com.mixc.smartconcierge.service.ComplaintAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/complaint")
@RequiredArgsConstructor
public class ComplaintAdminController {

    private final ComplaintAdminService complaintAdminService;
    private final AuditLogWriter auditLogWriter;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('客服','管理员')")
    public ApiResult<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(complaintAdminService.list(status, type, start, end, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('客服','管理员')")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResult.ok(complaintAdminService.detail(id));
    }

    @GetMapping("/phone/{id}")
    @PreAuthorize("hasAnyRole('客服','管理员')")
    public ApiResult<String> phone(@PathVariable Long id) {
        String phone = complaintAdminService.viewPhone(id);
        auditLogWriter.write("查看了 工单#" + id + " 的完整手机号", "工单");
        return ApiResult.ok(phone);
    }

    @PostMapping("/handle")
    @PreAuthorize("hasAnyRole('客服','管理员')")
    public ApiResult<Void> handle(@RequestBody ComplaintHandleRequest request) {
        complaintAdminService.handle(request);
        return ApiResult.ok();
    }
}
