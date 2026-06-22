package com.mixc.smartconcierge.controller.api;

import com.mixc.smartconcierge.dto.ChatConverseRequest;
import com.mixc.smartconcierge.dto.RecommendRequest;
import com.mixc.smartconcierge.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final RecommendService recommendService;

    @PostMapping("/chat/converse")
    public Map<String, Object> converse(@RequestBody ChatConverseRequest request,
                                          @RequestHeader(value = "X-Openid", required = false) String openid) {
        return recommendService.converse(request, openid);
    }

    @PostMapping("/chat/recommend")
    public Map<String, Object> recommend(@RequestBody RecommendRequest request,
                                         @RequestHeader(value = "X-Openid", required = false) String openid) {
        return recommendService.recommend(request, openid);
    }
}
