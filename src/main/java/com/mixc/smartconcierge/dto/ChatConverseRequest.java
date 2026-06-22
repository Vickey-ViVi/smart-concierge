package com.mixc.smartconcierge.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatConverseRequest {
    private String type;
    private String companions;
    private String message;
    private List<Map<String, String>> history;
}
