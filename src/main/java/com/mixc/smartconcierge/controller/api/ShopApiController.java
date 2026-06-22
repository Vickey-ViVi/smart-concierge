package com.mixc.smartconcierge.controller.api;

import com.mixc.smartconcierge.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopApiController {

    private final ShopService shopService;

    @GetMapping("/list")
    public Object list() {
        return shopService.listActive();
    }
}
