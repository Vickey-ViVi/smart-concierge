package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.common.BusinessException;
import com.mixc.smartconcierge.common.PageResult;
import com.mixc.smartconcierge.dto.ComplaintHandleRequest;
import com.mixc.smartconcierge.entity.Complaint;
import com.mixc.smartconcierge.entity.ComplaintLog;
import com.mixc.smartconcierge.entity.LostFound;
import com.mixc.smartconcierge.repository.ComplaintLogRepository;
import com.mixc.smartconcierge.repository.ComplaintRepository;
import com.mixc.smartconcierge.repository.LostFoundRepository;
import com.mixc.smartconcierge.security.AdminUserDetailsService;
import com.mixc.smartconcierge.util.CryptoUtil;
import com.mixc.smartconcierge.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintAdminService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintLogRepository complaintLogRepository;
    private final LostFoundRepository lostFoundRepository;
    private final CryptoUtil cryptoUtil;
    private final AdminUserDetailsService adminUserDetailsService;

    public PageResult<Map<String, Object>> list(Integer status, String type,
                                                 LocalDateTime start, LocalDateTime end,
                                                 int pageNum, int pageSize) {
        Page<Complaint> page = complaintRepository.search(status, type, start, end,
                PageRequest.of(Math.max(pageNum - 1, 0), pageSize));
        List<Map<String, Object>> list = page.getContent().stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotalElements(), list);
    }

    public Map<String, Object> detail(Long id) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工单不存在"));
        Map<String, Object> map = toListItem(c);
        map.put("description", c.getDescription());
        map.put("imageUrls", JsonUtil.parseList(c.getImageUrls()));
        map.put("logs", complaintLogRepository.findByComplaintIdOrderByCreateTimeAsc(id).stream()
                .map(this::toLogItem).collect(Collectors.toList()));
        return map;
    }

    public String viewPhone(Long id) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工单不存在"));
        return cryptoUtil.decryptPhone(c.getPhone());
    }

    @Transactional
    public void handle(ComplaintHandleRequest req) {
        Complaint c = complaintRepository.findById(req.getId())
                .orElseThrow(() -> new BusinessException("工单不存在"));
        String operator = currentRealName();
        String action = req.getAction();
        switch (action) {
            case "accept" -> {
                if (c.getStatus() != 0) {
                    throw new BusinessException("仅待处理工单可接单");
                }
                c.setStatus(1);
                c.setAssignee(operator);
                saveLog(c.getId(), operator, "接单", req.getRemark());
            }
            case "transfer" -> {
                if (c.getStatus() != 1) {
                    throw new BusinessException("仅处理中工单可转交");
                }
                c.setStatus(0);
                c.setAssignee(null);
                saveLog(c.getId(), operator, "转交", req.getRemark());
            }
            case "complete" -> {
                if (c.getStatus() != 1) {
                    throw new BusinessException("仅处理中工单可完成");
                }
                c.setStatus(2);
                saveLog(c.getId(), operator, "完成", req.getRemark());
            }
            default -> throw new BusinessException("未知操作: " + action);
        }
        complaintRepository.save(c);
    }

    @Transactional
    public LostFound addLostFound(LostFound item) {
        item.setStatus(0);
        return lostFoundRepository.save(item);
    }

    public List<LostFound> listAll() {
        return lostFoundRepository.findAll().stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
    }

    public List<LostFound> matchLost(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException("工单不存在"));
        if (!"lost".equals(c.getType())) {
            return List.of();
        }
        String keyword = extractKeyword(c.getDescription());
        if (keyword.isBlank()) {
            return lostFoundRepository.findAll().stream()
                    .filter(l -> l.getStatus() == 0)
                    .limit(10)
                    .collect(Collectors.toList());
        }
        return lostFoundRepository.matchByKeyword(keyword);
    }

    private String extractKeyword(String description) {
        if (description == null) {
            return "";
        }
        String[] keywords = {"钱包", "手机", "钥匙", "身份证", "包", "耳机", "雨伞", "证件"};
        for (String k : keywords) {
            if (description.contains(k)) {
                return k;
            }
        }
        return description.length() > 4 ? description.substring(0, 4) : description;
    }

    private void saveLog(Long complaintId, String operator, String action, String remark) {
        ComplaintLog log = new ComplaintLog();
        log.setComplaintId(complaintId);
        log.setOperator(operator);
        log.setAction(action);
        log.setRemark(remark);
        complaintLogRepository.save(log);
    }

    private Map<String, Object> toListItem(Complaint c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("ticketNo", "TK" + c.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", c.getId()));
        map.put("type", c.getType());
        map.put("descriptionSummary", c.getDescription() != null && c.getDescription().length() > 30
                ? c.getDescription().substring(0, 30) + "..." : c.getDescription());
        map.put("phoneMasked", cryptoUtil.maskPhone(cryptoUtil.decryptPhone(c.getPhone())));
        map.put("status", c.getStatus());
        map.put("assignee", c.getAssignee());
        map.put("createTime", c.getCreateTime());
        return map;
    }

    private Map<String, Object> toLogItem(ComplaintLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("operator", log.getOperator());
        map.put("action", log.getAction());
        map.put("remark", log.getRemark());
        map.put("createTime", log.getCreateTime());
        return map;
    }

    private String currentRealName() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminUserDetailsService.loadEntity(username).getRealName();
    }
}
