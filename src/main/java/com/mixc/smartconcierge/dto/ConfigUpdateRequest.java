package com.mixc.smartconcierge.dto;

import lombok.Data;

@Data
public class ConfigUpdateRequest {
    private String configKey;
    private String configValue;
}
