package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.dto.ChatConverseRequest;
import com.mixc.smartconcierge.dto.RecommendRequest;
import com.mixc.smartconcierge.dto.ShopVO;
import com.mixc.smartconcierge.entity.QuestionLog;
import com.mixc.smartconcierge.entity.Shop;
import com.mixc.smartconcierge.repository.QuestionLogRepository;
import com.mixc.smartconcierge.repository.ShopRepository;
import com.mixc.smartconcierge.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ShopRepository shopRepository;
    private final QuestionLogRepository questionLogRepository;
    private final UserSessionService userSessionService;
    private final DeepseekService deepseekService;

    private static final Map<String, String> TYPE_LABEL = Map.of(
            "food", "美食",
            "entertainment", "玩乐",
            "shopping", "购物",
            "gift", "购物"
    );

    private static final Map<String, String> TYPE_CATEGORY = Map.of(
            "food", "美食",
            "entertainment", "娱乐",
            "shopping", "服饰",
            "gift", "服饰"
    );

    @Transactional
    public Map<String, Object> converse(ChatConverseRequest req, String openidHeader) {
        long start = System.currentTimeMillis();
        String openid = userSessionService.resolveOpenid(openidHeader);
        String systemPrompt = buildChatSystemPrompt(req.getType(), req.getCompanions());
        String reply = null;
        boolean usedAi = false;
        if (deepseekService.isEnabled()) {
            reply = deepseekService.chat(systemPrompt, req.getHistory(), req.getMessage());
            usedAi = reply != null;
        }
        if (reply == null) {
            reply = "好的，我已了解您的需求。请继续补充偏好，或点击「获取店铺推荐」为您精选商场店铺。";
        }

        QuestionLog log = new QuestionLog();
        log.setOpenid(openid);
        log.setIntent(req.getType());
        log.setQuestionText(req.getMessage());
        log.setAnswerText(reply);
        log.setUseDeepseek(usedAi ? 1 : 0);
        log.setResponseTimeMs((int) (System.currentTimeMillis() - start));
        questionLogRepository.save(log);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("reply", reply);
        result.put("useDeepseek", usedAi);
        return result;
    }

    @Transactional
    public Map<String, Object> recommend(RecommendRequest req, String openidHeader) {
        long start = System.currentTimeMillis();
        String openid = userSessionService.resolveOpenid(openidHeader);
        String category = TYPE_CATEGORY.getOrDefault(req.getType(), null);
        List<Shop> candidates = shopRepository.searchActive(category, null);
        if (candidates.isEmpty()) {
            candidates = shopRepository.findByStatusOrderByLikeCountDesc(1);
        }

        List<ShopVO> recommendations;
        boolean usedAi = false;
        if (deepseekService.isEnabled()) {
            List<ShopVO> aiResult = recommendWithDeepseek(req, candidates);
            if (aiResult != null && !aiResult.isEmpty()) {
                recommendations = aiResult;
                usedAi = true;
            } else {
                recommendations = ruleRecommend(req, candidates);
            }
            usedAi = usedAi || enrichReasonsWithDeepseek(req, recommendations, candidates, usedAi);
        } else {
            recommendations = ruleRecommend(req, candidates);
        }

        QuestionLog log = new QuestionLog();
        log.setOpenid(openid);
        log.setIntent(req.getType());
        log.setQuestionText(buildQuestionText(req));
        log.setAnswerText(recommendations.stream().map(ShopVO::getName).collect(Collectors.joining("、")));
        log.setRecommendDetail(JsonUtil.toJson(recommendations));
        log.setUseDeepseek(usedAi ? 1 : 0);
        log.setResponseTimeMs((int) (System.currentTimeMillis() - start));
        questionLogRepository.save(log);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("type", req.getType());
        result.put("recommendations", recommendations);
        result.put("useDeepseek", usedAi);
        result.put("logId", log.getId());
        return result;
    }

    private List<ShopVO> recommendWithDeepseek(RecommendRequest req, List<Shop> candidates) {
        String typeLabel = TYPE_LABEL.getOrDefault(req.getType(), req.getType());
        String systemPrompt = """
                你是南通万象城智慧导购助手。根据顾客需求和商场店铺清单，推荐最多5家最合适的店铺。
                只能从提供的店铺清单中选择，不要编造不存在的店铺。
                每家店铺的 reason 必须个性化：结合顾客对话中的具体偏好，提及该店品类/标签/人均等特色，各店理由不得相同。
                禁止输出「人气较高、符合商场定位」等模板化空话。
                必须返回 JSON 格式：
                {"recommendations":[{"shopId":数字,"reason":"推荐理由30-50字"}]}
                店铺清单：
                """ + deepseekService.buildShopCatalog(candidates);

        String userPrompt = buildRecommendUserPrompt(req, typeLabel);
        List<Map<String, Object>> parsed = deepseekService.recommendShops(systemPrompt, userPrompt);
        if (parsed == null || parsed.isEmpty()) {
            return null;
        }

        Map<Long, Shop> shopMap = candidates.stream().collect(Collectors.toMap(Shop::getId, s -> s, (a, b) -> a));
        List<ShopVO> list = new ArrayList<>();
        for (Map<String, Object> item : parsed) {
            Long shopId = toLong(item.get("shopId"));
            if (shopId == null) {
                continue;
            }
            Shop shop = shopMap.get(shopId);
            if (shop == null) {
                continue;
            }
            ShopVO vo = toVO(shop);
            vo.setReason(String.valueOf(item.getOrDefault("reason", "为您精选推荐")));
            list.add(vo);
            if (list.size() >= 5) {
                break;
            }
        }
        return list.isEmpty() ? null : list;
    }

    private boolean enrichReasonsWithDeepseek(RecommendRequest req, List<ShopVO> recommendations,
                                           List<Shop> candidates, boolean alreadyHasAiReasons) {
        if (recommendations == null || recommendations.isEmpty()) {
            return false;
        }
        boolean allGeneric = recommendations.stream()
                .allMatch(v -> v.getReason() == null || v.getReason().contains("符合商场定位"));
        if (alreadyHasAiReasons && !allGeneric) {
            return true;
        }
        Map<Long, Shop> shopMap = candidates.stream()
                .collect(Collectors.toMap(Shop::getId, s -> s, (a, b) -> a));
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append(buildRecommendUserPrompt(req, TYPE_LABEL.getOrDefault(req.getType(), req.getType())));
        userPrompt.append("\n已选店铺：\n");
        for (ShopVO vo : recommendations) {
            Shop shop = shopMap.get(vo.getId());
            if (shop != null) {
                userPrompt.append(String.format("- shopId:%d 名称:%s 楼层:%s 标签:%s 人均:%s%n",
                        shop.getId(), shop.getName(), shop.getFloor(), shop.getTags(), shop.getAvgPrice()));
            }
        }
        Map<Long, String> reasons = deepseekService.generateShopReasons(userPrompt.toString());
        if (reasons == null) {
            return false;
        }
        for (ShopVO vo : recommendations) {
            String reason = reasons.get(vo.getId());
            if (reason != null && !reason.isBlank()) {
                vo.setReason(reason);
            }
        }
        return true;
    }

    private List<ShopVO> ruleRecommend(RecommendRequest req, List<Shop> candidates) {
        List<Shop> filtered = candidates.stream()
                .sorted(Comparator.comparing(Shop::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());
        String typeLabel = TYPE_LABEL.getOrDefault(req.getType(), "精选");
        return filtered.stream().map(shop -> {
            ShopVO vo = toVO(shop);
            vo.setReason(String.format("根据您选择的%s场景及%d人同行需求，该店铺人气较高、符合商场定位。",
                    typeLabel, parseCompanionCount(req.getCompanions())));
            return vo;
        }).collect(Collectors.toList());
    }

    private String buildChatSystemPrompt(String type, String companions) {
        String typeLabel = TYPE_LABEL.getOrDefault(type, type);
        return String.format("""
                你是南通万象城（MixC）智慧导购助手，语气亲切专业。
                顾客已选择咨询类型：%s，同行人数：%s。
                请根据顾客后续描述，追问或解答关于逛店、就餐、购物的问题。
                回答简洁，80字以内为宜，不要使用 markdown。
                若信息已足够，可提示顾客点击「获取店铺推荐」生成精选店铺列表。
                """, typeLabel, companions != null ? companions : "未说明");
    }

    private String buildRecommendUserPrompt(RecommendRequest req, String typeLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append("咨询类型：").append(typeLabel).append("\n");
        sb.append("同行人数：").append(req.getCompanions()).append("\n");
        if (req.getMessages() != null && !req.getMessages().isEmpty()) {
            sb.append("对话记录：\n");
            for (Map<String, String> m : req.getMessages()) {
                sb.append(m.get("role")).append(": ").append(m.get("content")).append("\n");
            }
        }
        sb.append("请推荐最合适的店铺并说明理由。");
        return sb.toString();
    }

    private int parseCompanionCount(String companions) {
        if (companions == null) {
            return 1;
        }
        if (companions.contains("3") || companions.contains("以上")) {
            return 3;
        }
        if (companions.contains("2")) {
            return 2;
        }
        return 1;
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildQuestionText(RecommendRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("type=%s, companions=%s", req.getType(), req.getCompanions()));
        if (req.getMessages() != null) {
            sb.append(", messages=").append(req.getMessages().size());
        }
        return sb.toString();
    }

    public ShopVO toVO(Shop shop) {
        ShopVO vo = new ShopVO();
        vo.setId(shop.getId());
        vo.setName(shop.getName());
        vo.setFloor(shop.getFloor());
        vo.setCategory(shop.getCategory());
        vo.setSubCategory(shop.getSubCategory());
        vo.setAvgPrice(shop.getAvgPrice());
        vo.setDiscountInfo(shop.getDiscountInfo());
        if (shop.getTags() != null) {
            vo.setTags(shop.getTags().replace("[", "").replace("]", "").replace("\"", ""));
        }
        return vo;
    }
}
