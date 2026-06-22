package com.mixc.smartconcierge.controller.admin;

import com.mixc.smartconcierge.common.ApiResult;
import com.mixc.smartconcierge.dto.ProposalThresholdRequest;
import com.mixc.smartconcierge.service.ProposalExportService;
import com.mixc.smartconcierge.service.ProposalService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/proposal")
@RequiredArgsConstructor
public class ProposalAdminController {

    private final ProposalService proposalService;
    private final ProposalExportService proposalExportService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('招商','管理员')")
    public ApiResult<List<Map<String, Object>>> list() {
        return ApiResult.ok(proposalService.listAll());
    }

    @PutMapping("/threshold")
    @PreAuthorize("hasAnyRole('招商','管理员')")
    public ApiResult<Void> threshold(@RequestBody ProposalThresholdRequest request) {
        proposalService.updateThreshold(request.getBrandName(), request.getThreshold(), request.getGlobalThreshold());
        return ApiResult.ok();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('招商','管理员')")
    public void export(HttpServletResponse response) throws IOException {
        proposalExportService.exportOverThreshold(response);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('招商','管理员')")
    public ApiResult<com.mixc.smartconcierge.common.PageResult<Map<String, Object>>> records(
            @RequestParam(required = false) String brandName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(proposalService.listRecords(brandName, pageNum, pageSize));
    }
}
