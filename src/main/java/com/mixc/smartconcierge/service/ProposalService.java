package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.entity.Proposal;
import com.mixc.smartconcierge.entity.ProposalRecord;
import com.mixc.smartconcierge.repository.ProposalRecordRepository;
import com.mixc.smartconcierge.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProposalRecordRepository proposalRecordRepository;
    private final SystemConfigService systemConfigService;
    private final UserSessionService userSessionService;

    @Transactional
    public Map<String, Object> addProposal(String brand, String reason, String openidHeader) {
        if (brand == null || brand.isBlank()) {
            return Map.of("success", false, "message", "品牌名称不能为空");
        }
        if (reason == null || reason.isBlank()) {
            return Map.of("success", false, "message", "请填写推荐理由");
        }
        String openid = userSessionService.resolveOpenid(openidHeader);
        String normalized = brand.trim();
        Optional<Proposal> existing = proposalRepository.findByBrandName(normalized);
        if (existing.isPresent()) {
            Proposal p = existing.get();
            return Map.of(
                    "success", true,
                    "isNew", false,
                    "existing", true,
                    "count", p.getCount(),
                    "proposalCount", p.getCount(),
                    "message", "已有相同提议"
            );
        }
        Proposal proposal = new Proposal();
        proposal.setBrandName(normalized);
        proposal.setCount(1);
        proposal.setThreshold(systemConfigService.getInt("proposal_global_threshold", 100));
        proposal.setNotified(0);
        proposalRepository.save(proposal);
        saveRecord(openid, normalized, reason.trim());
        checkThreshold(proposal);
        return Map.of("success", true, "isNew", true, "count", 1);
    }

    @Transactional
    public Map<String, Object> likeProposal(String brand) {
        Proposal p = proposalRepository.findByBrandName(brand.trim())
                .orElseThrow(() -> new NoSuchElementException("品牌不存在"));
        p.setCount(p.getCount() + 1);
        p.setLastProposeTime(LocalDateTime.now());
        proposalRepository.save(p);
        checkThreshold(p);
        return Map.of("success", true, "count", p.getCount());
    }

    public List<Map<String, Object>> hotProposals() {
        return proposalRepository.findTop10ByOrderByCountDesc().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listAll() {
        return proposalRepository.findAll().stream()
                .sorted(Comparator.comparing(Proposal::getCount).reversed())
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateThreshold(String brandName, Integer threshold, Integer globalThreshold) {
        if (globalThreshold != null) {
            systemConfigService.update("proposal_global_threshold", String.valueOf(globalThreshold));
        }
        if (brandName != null && !brandName.isBlank() && threshold != null) {
            Proposal p = proposalRepository.findByBrandName(brandName)
                    .orElseThrow(() -> new NoSuchElementException("品牌不存在"));
            p.setThreshold(threshold);
            proposalRepository.save(p);
        }
    }

    public List<Proposal> overThresholdList() {
        int global = systemConfigService.getInt("proposal_global_threshold", 100);
        return proposalRepository.findOverThreshold(global);
    }

    public com.mixc.smartconcierge.common.PageResult<Map<String, Object>> listRecords(String brandName, int pageNum, int pageSize) {
        var page = proposalRecordRepository.search(brandName,
                org.springframework.data.domain.PageRequest.of(Math.max(pageNum - 1, 0), pageSize));
        List<Map<String, Object>> list = page.getContent().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("brandName", r.getBrandName());
            m.put("reason", r.getReason());
            m.put("openid", maskOpenid(r.getOpenid()));
            m.put("createTime", r.getCreateTime());
            return m;
        }).collect(Collectors.toList());
        return new com.mixc.smartconcierge.common.PageResult<>(page.getTotalElements(), list);
    }

    private void saveRecord(String openid, String brandName, String reason) {
        ProposalRecord record = new ProposalRecord();
        record.setOpenid(openid);
        record.setBrandName(brandName);
        record.setReason(reason);
        proposalRecordRepository.save(record);
    }

    private String maskOpenid(String openid) {
        if (openid == null || openid.length() < 8) {
            return openid;
        }
        return openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
    }

    private void checkThreshold(Proposal p) {
        int threshold = p.getThreshold() != null ? p.getThreshold()
                : systemConfigService.getInt("proposal_global_threshold", 100);
        if (p.getCount() >= threshold && (p.getNotified() == null || p.getNotified() == 0)) {
            p.setNotified(1);
            proposalRepository.save(p);
        }
    }

    private Map<String, Object> toMap(Proposal p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("brand", p.getBrandName());
        map.put("brandName", p.getBrandName());
        map.put("count", p.getCount());
        map.put("threshold", p.getThreshold());
        map.put("notified", p.getNotified());
        map.put("lastProposeTime", p.getLastProposeTime());
        return map;
    }
}
