package com.mixc.smartconcierge.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShopSaveRequest {
    private Long id;
    private String name;
    private String floor;
    private String category;
    private String subCategory;
    private String tags;
    private Integer avgPrice;
    private String discountInfo;
    private Long activityId;
    private Integer likeCount;
    private Integer status;
}
