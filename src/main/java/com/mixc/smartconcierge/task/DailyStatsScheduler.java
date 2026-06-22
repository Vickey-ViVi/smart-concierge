package com.mixc.smartconcierge.task;

import com.mixc.smartconcierge.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatsScheduler {

    private final DashboardService dashboardService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        dashboardService.computeDailyStats(yesterday);
        dashboardService.computeDailyStats(LocalDate.now());
        log.info("Daily stats computed for {}", yesterday);
    }
}
