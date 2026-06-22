package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.entity.QuestionLog;
import com.mixc.smartconcierge.repository.QuestionLogRepository;
import com.mixc.smartconcierge.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionLogAdminService {

    private final QuestionLogRepository questionLogRepository;

    private static final Map<String, String> INTENT_LABEL = Map.of(
            "food", "美食",
            "entertainment", "玩乐",
            "shopping", "购物",
            "gift", "购物",
            "family", "亲子",
            "route", "路线"
    );

    public PageResult<Map<String, Object>> list(String intent, LocalDateTime start, LocalDateTime end,
                                                   int pageNum, int pageSize) {
        Page<QuestionLog> page = questionLogRepository.search(intent, start, end,
                PageRequest.of(Math.max(pageNum - 1, 0), pageSize));
        List<Map<String, Object>> list = page.getContent().stream().map(this::toView).toList();
        return new PageResult<>(page.getTotalElements(), list);
    }

    public Map<String, Object> detail(Long id) {
        QuestionLog log = questionLogRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("记录不存在"));
        return toView(log);
    }

    private Map<String, Object> toView(QuestionLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("intent", log.getIntent());
        map.put("intentLabel", INTENT_LABEL.getOrDefault(log.getIntent(), log.getIntent()));
        map.put("questionText", log.getQuestionText());
        map.put("answerText", log.getAnswerText());
        map.put("useDeepseek", log.getUseDeepseek());
        map.put("responseTimeMs", log.getResponseTimeMs());
        map.put("createTime", log.getCreateTime());
        map.put("recommendations", JsonUtil.parseList(log.getRecommendDetail()));
        return map;
    }
}
