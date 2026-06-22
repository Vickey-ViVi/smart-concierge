package com.mixc.smartconcierge.dto;

import lombok.Data;

@Data
public class ShopVO {
    private Long id;
    private String name;
    private String floor;
    private String category;
    private String subCategory;
    private String tags;
    private Integer avgPrice;
    private String discountInfo;
    private String reason;
}
