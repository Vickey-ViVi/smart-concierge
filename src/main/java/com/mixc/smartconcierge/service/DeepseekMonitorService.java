package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.repository.QuestionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepseekMonitorService {

    private final DeepseekService deepseekService;
    private final SystemConfigService systemConfigService;
    private final QuestionLogRepository questionLogRepository;

    public Map<String, Object> buildStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        long todayCalls = questionLogRepository.countDeepseekSuccessBetween(todayStart, tomorrowStart);
        long monthCalls = questionLogRepository.countDeepseekSuccessBetween(monthStart, nextMonthStart);
        long todayTotal = questionLogRepository.countBetween(todayStart, tomorrowStart);
        long monthTotal = questionLogRepository.countBetween(monthStart, nextMonthStart);

        Double todayAvg = questionLogRepository.avgDeepseekLatencyBetween(todayStart, tomorrowStart);
        Double monthAvg = questionLogRepository.avgDeepseekLatencyBetween(monthStart, nextMonthStart);
        int avgLatencyMs = resolveAvgLatency(todayAvg, monthAvg);

        boolean switchOn = deepseekService.isSwitchOn();
        boolean keyConfigured = deepseekService.hasApiKey();
        String keySource = deepseekService.getKeySource();
        boolean dbSwitchOn = systemConfigService.getBoolean("deepseek_enabled", false);
        boolean dbKeyConfigured = !systemConfigService.get("deepseek_api_key", "").isBlank();

        Status status = resolveStatus(switchOn, keyConfigured, todayCalls, monthCalls, todayTotal, monthTotal);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("status", status.code);
        stats.put("statusLabel", status.label);
        stats.put("statusHint", status.hint);
        stats.put("enabled", status.actuallyWorking);
        stats.put("switchOn", switchOn);
        stats.put("keyConfigured", keyConfigured);
        stats.put("keySource", keySource);
        stats.put("keySourceLabel", keySourceLabel(keySource));
        stats.put("dbSwitchOn", dbSwitchOn);
        stats.put("dbKeyConfigured", dbKeyConfigured);
        stats.put("todayCalls", todayCalls);
        stats.put("monthCalls", monthCalls);
        stats.put("todayTotal", todayTotal);
        stats.put("monthTotal", monthTotal);
        stats.put("avgLatencyMs", avgLatencyMs);
        stats.put("successRate", formatSuccessRate(monthCalls, monthTotal));
        return stats;
    }

    private Status resolveStatus(boolean switchOn, boolean keyConfigured,
                                 long todaySuccess, long monthSuccess,
                                 long todayTotal, long monthTotal) {
        if (!switchOn) {
            return new Status("disabled", "已关闭",
                    "配置开关未开启，智能提问将使用规则兜底。", false);
        }
        if (!keyConfigured) {
            return new Status("no_key", "未配置 Key",
                    "请在系统配置或 application-local.properties 中填写 deepseek_api_key。", false);
        }
        if (monthSuccess > 0 || todaySuccess > 0) {
            return new Status("active", "运行中",
                    "Deepseek 已成功参与推荐/对话。", true);
        }
        if (todayTotal > 0 || monthTotal > 0) {
            return new Status("ineffective", "调用未生效",
                    "开关与 Key 已配置，但近期请求均未成功调用 Deepseek。请检查 API Key 是否正确、Deepseek 账户是否有余额。", false);
        }
        return new Status("idle", "已配置待调用",
                "Key 已就绪，尚未产生成功调用记录。完成一次智能提问后即可在此查看统计。", false);
    }

    private int resolveAvgLatency(Double todayAvg, Double monthAvg) {
        if (todayAvg != null && todayAvg > 0) {
            return todayAvg.intValue();
        }
        if (monthAvg != null && monthAvg > 0) {
            return monthAvg.intValue();
        }
        return systemConfigService.getInt("deepseek_timeout", 60000);
    }

    private String formatSuccessRate(long success, long total) {
        if (total <= 0) {
            return "—";
        }
        int pct = (int) Math.round(success * 100.0 / total);
        return pct + "%";
    }

    private String keySourceLabel(String source) {
        return switch (source) {
            case "system_config" -> "系统配置";
            case "local_file" -> "本地配置文件";
            default -> "未配置";
        };
    }

    private record Status(String code, String label, String hint, boolean actuallyWorking) {}
}
