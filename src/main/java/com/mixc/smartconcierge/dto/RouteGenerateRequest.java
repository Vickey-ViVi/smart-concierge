package com.mixc.smartconcierge.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteGenerateRequest {
    private List<Long> shopIds;
    private String naturalInput;
    private Integer timeLimit;
    private Boolean hasStroller;
    private Boolean hasWheelchair;
}
