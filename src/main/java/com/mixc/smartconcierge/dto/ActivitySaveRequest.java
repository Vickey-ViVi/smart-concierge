package com.mixc.smartconcierge.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySaveRequest {
    private Long id;
    private String title;
    private String bannerUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String description;
    private Integer status;
}
