package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.dto.ShopSaveRequest;
import com.mixc.smartconcierge.entity.Shop;
import com.mixc.smartconcierge.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/shop")
@RequiredArgsConstructor
public class ShopAdminController {

    private final ShopService shopService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<PageResult<com.mixc.smartconcierge.dto.ShopVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(shopService.page(keyword, pageNum, pageSize));
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<Shop> save(@RequestBody ShopSaveRequest request) {
        return ApiResult.ok(shopService.save(request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        shopService.delete(id);
        return ApiResult.ok();
    }
}
