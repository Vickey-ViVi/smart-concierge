package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.dto.ActivitySaveRequest;
import com.mixc.smartconcierge.entity.Activity;
import com.mixc.smartconcierge.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
public class ActivityAdminController {

    private final ActivityService activityService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<PageResult<Activity>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(activityService.page(keyword, pageNum, pageSize));
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<Activity> save(@RequestBody ActivitySaveRequest request) {
        return ApiResult.ok(activityService.save(request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return ApiResult.ok();
    }
}
