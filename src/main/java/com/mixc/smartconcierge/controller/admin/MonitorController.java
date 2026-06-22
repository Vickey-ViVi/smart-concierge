package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.service.DeepseekMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final DeepseekMonitorService deepseekMonitorService;

    @GetMapping("/deepseek")
    @PreAuthorize("hasRole('管理员')")
    public ApiResult<Map<String, Object>> deepseekStats() {
        return ApiResult.ok(deepseekMonitorService.buildStats());
    }
}
