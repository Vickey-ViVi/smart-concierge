package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.common.BusinessException;
import com.mixc.smartconcierge.dto.ComplaintSubmitRequest;
import com.mixc.smartconcierge.entity.Complaint;
import com.mixc.smartconcierge.repository.ComplaintRepository;
import com.mixc.smartconcierge.util.CryptoUtil;
import com.mixc.smartconcierge.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComplaintApiService {

    private final ComplaintRepository complaintRepository;
    private final CryptoUtil cryptoUtil;
    private final UserSessionService userSessionService;

    @Transactional
    public Map<String, Object> submit(ComplaintSubmitRequest req, String openidHeader) {
        if (req.getType() == null || req.getType().isBlank()) {
            throw new BusinessException("请选择投诉类型");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new BusinessException("请填写描述");
        }

        String openid = userSessionService.resolveOpenid(openidHeader);
        Complaint complaint = new Complaint();
        complaint.setOpenid(openid);
        complaint.setType(req.getType());
        complaint.setDescription(req.getDescription());
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            complaint.setPhone(cryptoUtil.encryptPhone(req.getPhone()));
        }
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            complaint.setImageUrls(JsonUtil.toJson(req.getImages()));
        }
        complaint.setStatus(0);
        complaintRepository.save(complaint);

        String ticketNo = "TK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", complaint.getId());

        return Map.of(
                "code", 200,
                "ticketNo", ticketNo,
                "message", "提交成功"
        );
    }
}
