package com.mixc.smartconcierge.controller.api;

import com.mixc.smartconcierge.dto.ComplaintSubmitRequest;
import com.mixc.smartconcierge.service.ComplaintApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintApiController {

    private final ComplaintApiService complaintApiService;

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody ComplaintSubmitRequest request,
                                      @RequestHeader(value = "X-Openid", required = false) String openid) {
        return complaintApiService.submit(request, openid);
    }
}
