package com.mixc.smartconcierge.dto;

import lombok.Data;

@Data
public class ComplaintHandleRequest {
    private Long id;
    private String action;
    private String remark;
}
