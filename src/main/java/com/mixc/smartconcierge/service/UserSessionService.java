package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.entity.UserSession;
import com.mixc.smartconcierge.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    @Transactional
    public String resolveOpenid(String openidHeader) {
        if (openidHeader != null && !openidHeader.isBlank()) {
            ensureSession(openidHeader);
            return openidHeader;
        }
        String guest = "guest-" + System.currentTimeMillis();
        ensureSession(guest);
        return guest;
    }

    private void ensureSession(String openid) {
        userSessionRepository.findByOpenid(openid).orElseGet(() -> {
            UserSession session = new UserSession();
            session.setOpenid(openid);
            session.setNickname("访客");
            return userSessionRepository.save(session);
        });
        userSessionRepository.findByOpenid(openid).ifPresent(s -> {
            s.setLastActive(LocalDateTime.now());
            userSessionRepository.save(s);
        });
    }
}
