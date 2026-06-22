package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.dto.ConfigUpdateRequest;
import com.mixc.smartconcierge.entity.SystemConfig;
import com.mixc.smartconcierge.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class ConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('管理员')")
    public ApiResult<List<SystemConfig>> list() {
        return ApiResult.ok(systemConfigService.listAll());
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('管理员')")
    public ApiResult<Void> update(@RequestBody ConfigUpdateRequest request) {
        systemConfigService.update(request.getConfigKey(), request.getConfigValue());
        return ApiResult.ok();
    }
}
