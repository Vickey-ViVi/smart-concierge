package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.annotation.AuditLog;
import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.dto.AdminUserSaveRequest;
import com.mixc.smartconcierge.entity.AdminUser;
import com.mixc.smartconcierge.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('管理员')")
    public ApiResult<List<AdminUser>> list() {
        return ApiResult.ok(adminUserService.listAll());
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('管理员')")
    @AuditLog(action = "保存后台用户", module = "用户")
    public ApiResult<AdminUser> save(@RequestBody AdminUserSaveRequest request) {
        return ApiResult.ok(adminUserService.save(request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('管理员')")
    @AuditLog(action = "删除后台用户", module = "用户")
    public ApiResult<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ApiResult.ok();
    }

    @PostMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('管理员')")
    @AuditLog(action = "重置用户密码", module = "用户")
    public ApiResult<Map<String, String>> resetPassword(@PathVariable Long id) {
        return ApiResult.ok(adminUserService.resetPassword(id));
    }
}
