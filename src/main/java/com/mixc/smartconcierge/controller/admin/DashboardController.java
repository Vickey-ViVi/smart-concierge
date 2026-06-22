package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<Map<String, Object>> overview() {
        return ApiResult.ok(dashboardService.overview());
    }

    @GetMapping("/questions")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<List<Map<String, Object>>> questions() {
        return ApiResult.ok(dashboardService.topQuestions());
    }

    @GetMapping("/complaints")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<List<Map<String, Object>>> complaints() {
        return ApiResult.ok(dashboardService.topComplaints());
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<Map<String, Object>> trend() {
        return ApiResult.ok(dashboardService.trend());
    }
}
