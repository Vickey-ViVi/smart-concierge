package com.mixc.smartconcierge.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RecommendRequest {
    private String type;
    private String companions;
    private String relationship;
    private String avoid;
    private String special;
    /** 与 Deepseek 对话历史（step2 之后） */
    private List<Map<String, String>> messages;
}
