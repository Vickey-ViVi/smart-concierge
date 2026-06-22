package com.mixc.smartconcierge.dto;

import lombok.Data;

import java.util.List;

@Data
public class ComplaintSubmitRequest {
    private String type;
    private String description;
    private String phone;
    private List<String> images;
}
