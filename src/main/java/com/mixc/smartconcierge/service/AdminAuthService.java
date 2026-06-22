package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.dto.LoginRequest;
import com.mixc.smartconcierge.entity.AdminUser;
import com.mixc.smartconcierge.entity.LoginLog;
import com.mixc.smartconcierge.repository.AdminUserRepository;
import com.mixc.smartconcierge.repository.LoginLogRepository;
import com.mixc.smartconcierge.security.AdminUserDetailsService;
import com.mixc.smartconcierge.security.JwtTokenProvider;
import com.mixc.smartconcierge.util.RequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminUserDetailsService adminUserDetailsService;
    private final LoginLogRepository loginLogRepository;
    private final AdminUserRepository adminUserRepository;

    @Transactional
    public Map<String, Object> login(LoginRequest req) {
        LoginLog log = new LoginLog();
        log.setUsername(req.getUsername());
        log.setIpAddress(RequestUtil.getClientIp());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            AdminUser user = adminUserDetailsService.loadEntity(req.getUsername());
            user.setLastLogin(LocalDateTime.now());
            adminUserRepository.save(user);
            log.setSuccess(true);
            loginLogRepository.save(log);

            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole(), user.getRealName());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());
            result.put("username", user.getUsername());
            return result;
        } catch (Exception e) {
            log.setSuccess(false);
            loginLogRepository.save(log);
            throw e;
        }
    }
}
