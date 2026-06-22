package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.dto.LoginRequest;
import com.mixc.smartconcierge.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return ApiResult.ok(adminAuthService.login(request));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        return ApiResult.ok();
    }
}
