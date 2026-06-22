package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.service.QuestionLogAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/question")
@RequiredArgsConstructor
public class QuestionAdminController {

    private final QuestionLogAdminService questionLogAdminService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String intent,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(questionLogAdminService.list(intent, start, end, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('运营','管理员')")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResult.ok(questionLogAdminService.detail(id));
    }
}
