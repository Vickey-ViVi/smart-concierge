package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.dto.RouteGenerateRequest;
import com.mixc.smartconcierge.entity.IndoorNode;
import com.mixc.smartconcierge.entity.NodeEdge;
import com.mixc.smartconcierge.entity.Shop;
import com.mixc.smartconcierge.repository.IndoorNodeRepository;
import com.mixc.smartconcierge.repository.NodeEdgeRepository;
import com.mixc.smartconcierge.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final ShopRepository shopRepository;
    private final IndoorNodeRepository indoorNodeRepository;
    private final NodeEdgeRepository nodeEdgeRepository;
    private final DeepseekService deepseekService;

    public Map<String, Object> generate(RouteGenerateRequest req) {
        List<Shop> shops;
        if (req.getShopIds() != null && !req.getShopIds().isEmpty()) {
            shops = shopRepository.findAllById(req.getShopIds());
        } else if (req.getNaturalInput() != null && !req.getNaturalInput().isBlank()) {
            shops = shopRepository.searchActive(null, req.getNaturalInput()).stream().limit(5).collect(Collectors.toList());
        } else {
            shops = Collections.emptyList();
        }

        if (shops.size() < 2) {
            throw new IllegalArgumentException("至少需要2个有效店铺");
        }

        List<Map<String, Object>> steps = new ArrayList<>();
        int totalDistance = 0;
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("order", i + 1);
            step.put("shopId", shop.getId());
            step.put("shopName", shop.getName());
            step.put("floor", shop.getFloor());
            if (i > 0) {
                int dist = estimateDistance(shops.get(i - 1), shop);
                totalDistance += dist;
                step.put("distanceFromPrev", dist);
            }
            steps.add(step);
        }

        int estimatedMinutes = Math.min(req.getTimeLimit() != null ? req.getTimeLimit() : 120,
                Math.max(15, totalDistance / 50 + shops.size() * 10));

        Map<String, Object> route = new LinkedHashMap<>();
        route.put("steps", steps);
        route.put("totalDistance", totalDistance);
        route.put("estimatedMinutes", estimatedMinutes);
        route.put("hasStroller", Boolean.TRUE.equals(req.getHasStroller()));
        route.put("hasWheelchair", Boolean.TRUE.equals(req.getHasWheelchair()));
        route.put("summary", steps.stream().map(s -> (String) s.get("shopName")).collect(Collectors.joining(" → ")));

        boolean usedAi = false;
        if (deepseekService.isEnabled()) {
            usedAi = enrichRouteWithDeepseek(req, shops, steps, estimatedMinutes, totalDistance, route);
        }
        if (!usedAi) {
            route.put("reason", buildRuleRouteReason(req, shops, estimatedMinutes));
        }
        route.put("useDeepseek", usedAi);

        return Map.of("route", route);
    }

    private boolean enrichRouteWithDeepseek(RouteGenerateRequest req, List<Shop> shops,
                                            List<Map<String, Object>> steps, int estimatedMinutes,
                                            int totalDistance, Map<String, Object> route) {
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("路线顺序：").append(route.get("summary")).append("\n");
        userPrompt.append("预计总时长：").append(estimatedMinutes).append(" 分钟\n");
        userPrompt.append("步行距离约：").append(totalDistance).append(" 米\n");
        userPrompt.append("时长限制：").append(req.getTimeLimit() != null ? req.getTimeLimit() : 120).append(" 分钟\n");
        userPrompt.append("携带婴儿车：").append(Boolean.TRUE.equals(req.getHasStroller()) ? "是" : "否").append("\n");
        userPrompt.append("使用轮椅：").append(Boolean.TRUE.equals(req.getHasWheelchair()) ? "是" : "否").append("\n");
        if (req.getNaturalInput() != null && !req.getNaturalInput().isBlank()) {
            userPrompt.append("顾客描述：").append(req.getNaturalInput()).append("\n");
        }
        userPrompt.append("各站详情：\n");
        for (int i = 0; i < shops.size(); i++) {
            Shop s = shops.get(i);
            userPrompt.append(String.format("%d. %s %s %s%n", i + 1, s.getName(), s.getFloor(), s.getCategory()));
        }

        Map<String, Object> ai = deepseekService.generateRouteReason(userPrompt.toString());
        if (ai == null) {
            return false;
        }
        route.put("reason", ai.get("routeReason"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> aiSteps = (List<Map<String, Object>>) ai.get("steps");
        if (aiSteps != null) {
            for (Map<String, Object> aiStep : aiSteps) {
                int order = ((Number) aiStep.getOrDefault("order", 0)).intValue();
                String visitReason = String.valueOf(aiStep.getOrDefault("visitReason", ""));
                if (order > 0 && order <= steps.size() && !visitReason.isBlank()) {
                    steps.get(order - 1).put("reason", visitReason);
                    steps.get(order - 1).put("tip", visitReason);
                }
            }
        }
        return true;
    }

    private String buildRuleRouteReason(RouteGenerateRequest req, List<Shop> shops, int estimatedMinutes) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("按楼层由低到高串联 %d 个目的地，预计约 %d 分钟。", shops.size(), estimatedMinutes));
        if (Boolean.TRUE.equals(req.getHasStroller()) || Boolean.TRUE.equals(req.getHasWheelchair())) {
            sb.append("路线已优先选择电梯可达路径，减少台阶。");
        }
        return sb.toString();
    }

    private int estimateDistance(Shop from, Shop to) {
        Optional<IndoorNode> fromNode = indoorNodeRepository.findByShopId(from.getId());
        Optional<IndoorNode> toNode = indoorNodeRepository.findByShopId(to.getId());
        if (fromNode.isPresent() && toNode.isPresent()) {
            return shortestPathDistance(fromNode.get().getId(), toNode.get().getId());
        }
        if (Objects.equals(from.getFloor(), to.getFloor())) {
            return 80;
        }
        return 150;
    }

    private int shortestPathDistance(Long fromId, Long toId) {
        if (fromId.equals(toId)) {
            return 0;
        }
        Map<Long, List<NodeEdge>> graph = new HashMap<>();
        nodeEdgeRepository.findAll().forEach(edge -> {
            graph.computeIfAbsent(edge.getFromNode(), k -> new ArrayList<>()).add(edge);
            NodeEdge reverse = new NodeEdge();
            reverse.setFromNode(edge.getToNode());
            reverse.setToNode(edge.getFromNode());
            reverse.setDistance(edge.getDistance());
            graph.computeIfAbsent(edge.getToNode(), k -> new ArrayList<>()).add(reverse);
        });

        Map<Long, Integer> dist = new HashMap<>();
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));
        dist.put(fromId, 0);
        pq.add(new long[]{fromId, 0});
        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            long node = cur[0];
            int d = (int) cur[1];
            if (node == toId) {
                return d;
            }
            if (d > dist.getOrDefault(node, Integer.MAX_VALUE)) {
                continue;
            }
            for (NodeEdge edge : graph.getOrDefault(node, List.of())) {
                int nd = d + edge.getDistance();
                if (nd < dist.getOrDefault(edge.getToNode(), Integer.MAX_VALUE)) {
                    dist.put(edge.getToNode(), nd);
                    pq.add(new long[]{edge.getToNode(), nd});
                }
            }
        }
        return 120;
    }
}
