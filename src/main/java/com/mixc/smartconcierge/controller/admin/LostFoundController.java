package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.entity.LostFound;
import com.mixc.smartconcierge.service.ComplaintAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lost")
@RequiredArgsConstructor
public class LostFoundController {

    private final ComplaintAdminService complaintAdminService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('客服','管理员')")
    public ApiResult<LostFound> add(@RequestBody LostFound item) {
        return ApiResult.ok(complaintAdminService.addLostFound(item));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('客服','管理员')")
    public ApiResult<List<LostFound>> list() {
        return ApiResult.ok(complaintAdminService.listAll());
    }

    @GetMapping("/match/{complaintId}")
    @PreAuthorize("hasAnyRole('客服','管理员')")
    public ApiResult<List<LostFound>> match(@PathVariable Long complaintId) {
        return ApiResult.ok(complaintAdminService.matchLost(complaintId));
    }
}
