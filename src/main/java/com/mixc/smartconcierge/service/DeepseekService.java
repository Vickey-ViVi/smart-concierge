package com.mixc.smartconcierge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixc.smartconcierge.entity.Shop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepseekService {

    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.deepseek.api-key:}")
    private String defaultApiKey;

    @Value("${app.deepseek.base-url:https://api.deepseek.com}")
    private String defaultBaseUrl;

    @Value("${app.deepseek.enabled:false}")
    private boolean defaultEnabled;

    public boolean isEnabled() {
        boolean switchOn = defaultEnabled || systemConfigService.getBoolean("deepseek_enabled", false);
        return switchOn && hasApiKey();
    }

    public boolean hasApiKey() {
        String key = resolveApiKey();
        return key != null && !key.isBlank();
    }

    public boolean isSwitchOn() {
        return defaultEnabled || systemConfigService.getBoolean("deepseek_enabled", false);
    }

    /** Key 来源：system_config / local_file / none */
    public String getKeySource() {
        String fromConfig = systemConfigService.get("deepseek_api_key", "");
        if (fromConfig != null && !fromConfig.isBlank()) {
            return "system_config";
        }
        if (defaultApiKey != null && !defaultApiKey.isBlank()) {
            return "local_file";
        }
        return "none";
    }

    public String chat(String systemPrompt, List<Map<String, String>> history, String userMessage) {
        if (!isEnabled()) {
            return null;
        }
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            for (Map<String, String> h : history) {
                if (h.get("role") != null && h.get("content") != null) {
                    messages.add(Map.of("role", h.get("role"), "content", h.get("content")));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        return callApi(messages, false);
    }

    public List<Map<String, Object>> recommendShops(String systemPrompt, String userPrompt) {
        if (!isEnabled()) {
            return null;
        }
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        String raw = callApi(messages, true);
        return parseRecommendJson(raw);
    }

    /** 为已选店铺生成个性化推荐理由 */
    public Map<Long, String> generateShopReasons(String userPrompt) {
        if (!isEnabled()) {
            return null;
        }
        String systemPrompt = """
                你是南通万象城智慧导购助手。顾客已确定要推荐的店铺，请为每家店铺写一条独特的推荐理由。
                要求：
                1. 必须结合顾客对话中的具体需求（口味、预算、人数、场景、偏好等）
                2. 每条理由30-50字，提及该店铺名称或特色（楼层、标签、人均、品类）
                3. 各店理由不得雷同，禁止套用「人气较高、符合商场定位」等空泛模板
                4. 返回 JSON：{"reasons":[{"shopId":数字,"reason":"推荐理由"}]}
                """;
        String raw = callApi(List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ), true);
        return parseReasonMap(raw);
    }

    /** 为路线规划生成整体理由与各站说明 */
    public Map<String, Object> generateRouteReason(String userPrompt) {
        if (!isEnabled()) {
            return null;
        }
        String systemPrompt = """
                你是南通万象城室内路线规划助手。根据顾客选定的逛店顺序和偏好，给出规划说明。
                要求：
                1. routeReason：整体路线规划理由，80字以内，说明为何这样排序、如何兼顾时长与无障碍需求
                2. steps：为每一站写 visitReason（20-40字），说明该站停留价值或与前后站的衔接
                3. 语气亲切专业，不要使用 markdown
                4. 返回 JSON：{"routeReason":"...","steps":[{"order":1,"visitReason":"..."}]}
                """;
        String raw = callApi(List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ), true);
        return parseRouteReasonJson(raw);
    }

    private String callApi(List<Map<String, String>> messages, boolean jsonMode) {
        try {
            String apiKey = resolveApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                return null;
            }
            String baseUrl = systemConfigService.get("deepseek_base_url", defaultBaseUrl);
            int timeout = systemConfigService.getInt("deepseek_timeout", 60000);
            RestTemplate client = buildRestTemplate(timeout);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "deepseek-chat");
            body.put("messages", messages);
            body.put("temperature", 0.7);
            if (jsonMode) {
                body.put("response_format", Map.of("type", "json_object"));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = client.exchange(
                    baseUrl + "/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").path(0).path("message").path("content").asText(null);
        } catch (Exception e) {
            log.warn("Deepseek API call failed: {}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> parseRecommendJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode arr = root.has("recommendations") ? root.get("recommendations") : root;
            if (!arr.isArray()) {
                return null;
            }
            return objectMapper.convertValue(arr, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse Deepseek recommend JSON: {}", e.getMessage());
            return null;
        }
    }

    private Map<Long, String> parseReasonMap(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode arr = root.has("reasons") ? root.get("reasons") : root;
            if (!arr.isArray()) {
                return null;
            }
            Map<Long, String> map = new LinkedHashMap<>();
            for (JsonNode node : arr) {
                long shopId = node.path("shopId").asLong(-1);
                String reason = node.path("reason").asText(null);
                if (shopId > 0 && reason != null && !reason.isBlank()) {
                    map.put(shopId, reason.trim());
                }
            }
            return map.isEmpty() ? null : map;
        } catch (Exception e) {
            log.warn("Failed to parse Deepseek reason JSON: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> parseRouteReasonJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            String routeReason = root.path("routeReason").asText(null);
            if (routeReason == null || routeReason.isBlank()) {
                return null;
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("routeReason", routeReason.trim());
            List<Map<String, Object>> steps = new ArrayList<>();
            if (root.has("steps") && root.get("steps").isArray()) {
                for (JsonNode node : root.get("steps")) {
                    Map<String, Object> step = new LinkedHashMap<>();
                    step.put("order", node.path("order").asInt());
                    step.put("visitReason", node.path("visitReason").asText(""));
                    steps.add(step);
                }
            }
            result.put("steps", steps);
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse Deepseek route reason JSON: {}", e.getMessage());
            return null;
        }
    }

    private String resolveApiKey() {
        String fromConfig = systemConfigService.get("deepseek_api_key", "");
        if (fromConfig != null && !fromConfig.isBlank()) {
            return fromConfig.trim();
        }
        return defaultApiKey != null ? defaultApiKey.trim() : "";
    }

    private RestTemplate buildRestTemplate(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.min(timeoutMs, 15000));
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }

    public String buildShopCatalog(List<Shop> shops) {
        StringBuilder sb = new StringBuilder();
        for (Shop s : shops) {
            sb.append(String.format("- id:%d 名称:%s 楼层:%s 大类:%s 子类:%s 人均:%s 标签:%s 优惠:%s%n",
                    s.getId(), s.getName(), s.getFloor(), s.getCategory(),
                    s.getSubCategory(), s.getAvgPrice(), s.getTags(), s.getDiscountInfo()));
        }
        return sb.toString();
    }
}
