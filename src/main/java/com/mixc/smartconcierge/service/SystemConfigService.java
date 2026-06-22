package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.entity.SystemConfig;
import com.mixc.smartconcierge.repository.SystemConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadAll() {
        systemConfigRepository.findAll().forEach(c -> cache.put(c.getConfigKey(), c.getConfigValue()));
        ensureDefaults();
    }

    private void ensureDefaults() {
        putIfAbsent("proposal_global_threshold", "100", "全局提议阈值");
        putIfAbsent("complaint_alert_minutes", "5", "投诉预警时间窗口（分钟）");
        putIfAbsent("complaint_alert_count", "3", "窗口内触发预警的投诉次数");
        putIfAbsent("deepseek_timeout", "60000", "Deepseek API超时毫秒数");
        putIfAbsent("deepseek_enabled", "true", "是否启用Deepseek");
        putIfAbsent("deepseek_api_key", "", "Deepseek API Key");
        putIfAbsent("deepseek_base_url", "https://api.deepseek.com", "Deepseek API Base URL");
        putIfAbsent("complaint_alert_enabled", "true", "是否启用投诉预警");
    }

    private void putIfAbsent(String key, String value, String desc) {
        if (!cache.containsKey(key)) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(desc);
            systemConfigRepository.save(config);
            cache.put(key, value);
        }
    }

    public String get(String key, String defaultValue) {
        return cache.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    public List<SystemConfig> listAll() {
        return systemConfigRepository.findAll();
    }

    @Transactional
    public void update(String key, String value) {
        SystemConfig config = systemConfigRepository.findById(key).orElseGet(() -> {
            SystemConfig c = new SystemConfig();
            c.setConfigKey(key);
            return c;
        });
        config.setConfigValue(value);
        systemConfigRepository.save(config);
        cache.put(key, value);
    }
}
