package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.common.BusinessException;
import com.mixc.smartconcierge.dto.AdminUserSaveRequest;
import com.mixc.smartconcierge.entity.AdminUser;
import com.mixc.smartconcierge.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUser> listAll() {
        return adminUserRepository.findAll();
    }

    @Transactional
    public AdminUser save(AdminUserSaveRequest req) {
        AdminUser user = req.getId() != null
                ? adminUserRepository.findById(req.getId()).orElseThrow(() -> new BusinessException("用户不存在"))
                : new AdminUser();
        if (req.getId() == null && adminUserRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (req.getId() == null) {
            user.setUsername(req.getUsername());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        } else if (req.getId() == null) {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        user.setRealName(req.getRealName());
        user.setRole(req.getRole());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        return adminUserRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        adminUserRepository.deleteById(id);
    }

    @Transactional
    public Map<String, String> resetPassword(Long id) {
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        String temp = generateTempPassword();
        user.setPassword(passwordEncoder.encode(temp));
        adminUserRepository.save(user);
        return Map.of("tempPassword", temp);
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
