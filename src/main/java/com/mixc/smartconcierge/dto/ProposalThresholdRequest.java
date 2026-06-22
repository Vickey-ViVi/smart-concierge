package com.mixc.smartconcierge.dto;

import lombok.Data;

@Data
public class ProposalThresholdRequest {
    private String brandName;
    private Integer threshold;
    private Integer globalThreshold;
}
