package com.mixc.smartconcierge.controller.api;

import com.mixc.smartconcierge.dto.RouteGenerateRequest;
import com.mixc.smartconcierge.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteApiController {

    private final RouteService routeService;

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody RouteGenerateRequest request) {
        return routeService.generate(request);
    }
}
