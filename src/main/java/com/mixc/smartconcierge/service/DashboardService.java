package com.mixc.smartconcierge.service;

import com.mixc.smartconcierge.entity.DailyStats;
import com.mixc.smartconcierge.repository.ComplaintRepository;
import com.mixc.smartconcierge.repository.DailyStatsRepository;
import com.mixc.smartconcierge.repository.QuestionLogRepository;
import com.mixc.smartconcierge.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DailyStatsRepository dailyStatsRepository;
    private final QuestionLogRepository questionLogRepository;
    private final ComplaintRepository complaintRepository;
    private final ProposalService proposalService;

    public Map<String, Object> overview() {
        LocalDate today = LocalDate.now();
        DailyStats todayStats = dailyStatsRepository.findById(today).orElse(null);
        long pendingComplaints = complaintRepository.countByStatus(0);
        int overThreshold = proposalService.overThresholdList().size();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("todayUsers", todayStats != null ? todayStats.getTotalUsers() : countTodayUsers());
        map.put("pendingComplaints", pendingComplaints);
        map.put("overThresholdBrands", overThreshold);
        return map;
    }

    public List<Map<String, Object>> topQuestions() {
        return aggregateFromDailyOrLive("questions");
    }

    public List<Map<String, Object>> topComplaints() {
        return aggregateFromDailyOrLive("complaints");
    }

    public Map<String, Object> trend() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        List<DailyStats> stats = dailyStatsRepository.findByStatDateBetweenOrderByStatDateAsc(start, end);
        List<String> dates = new ArrayList<>();
        List<Integer> complaintCounts = new ArrayList<>();
        List<BigDecimal> satisfaction = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d.toString());
            LocalDate current = d;
            DailyStats day = stats.stream().filter(s -> s.getStatDate().equals(current)).findFirst().orElse(null);
            complaintCounts.add(day != null ? day.getComplaintCount() : 0);
            satisfaction.add(day != null && day.getSatisfactionIndex() != null
                    ? day.getSatisfactionIndex() : BigDecimal.valueOf(85));
        }
        return Map.of("dates", dates, "complaintCounts", complaintCounts, "satisfactionIndex", satisfaction);
    }

    @Transactional
    public void computeDailyStats(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        DailyStats stats = dailyStatsRepository.findById(date).orElse(new DailyStats());
        stats.setStatDate(date);
        stats.setTotalUsers((int) questionLogRepository.countDistinctOpenidBetween(start, end));

        List<Map<String, Object>> questions = questionLogRepository.countByIntentSince(start.minusDays(7)).stream()
                .limit(5)
                .map(row -> Map.<String, Object>of("intent", row[0], "count", row[1]))
                .collect(Collectors.toList());
        stats.setTopQuestions(JsonUtil.toJson(questions));

        List<Map<String, Object>> complaints = complaintRepository.countByTypeSince(start.minusDays(7)).stream()
                .limit(5)
                .map(row -> Map.<String, Object>of("type", row[0], "count", row[1]))
                .collect(Collectors.toList());
        stats.setTopComplaints(JsonUtil.toJson(complaints));

        long dayComplaints = complaintRepository.countByCreateTimeBetween(start, end);
        stats.setComplaintCount((int) dayComplaints);
        long completed = complaintRepository.countByStatus(2);
        long total = complaintRepository.count();
        BigDecimal satisfaction = total == 0 ? BigDecimal.valueOf(90)
                : BigDecimal.valueOf(100.0 * completed / total).setScale(2, RoundingMode.HALF_UP);
        stats.setSatisfactionIndex(satisfaction);
        dailyStatsRepository.save(stats);
    }

    private int countTodayUsers() {
        LocalDate today = LocalDate.now();
        return (int) questionLogRepository.countDistinctOpenidBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    private List<Map<String, Object>> aggregateFromDailyOrLive(String type) {
        LocalDate today = LocalDate.now();
        Optional<DailyStats> stats = dailyStatsRepository.findById(today);
        if (stats.isPresent()) {
            String json = "questions".equals(type) ? stats.get().getTopQuestions() : stats.get().getTopComplaints();
            return JsonUtil.parseList(json);
        }
        if ("questions".equals(type)) {
            return questionLogRepository.countByIntentSince(LocalDateTime.now().minusDays(7)).stream()
                    .limit(5)
                    .map(row -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("intent", row[0]);
                        m.put("count", row[1]);
                        return m;
                    }).collect(Collectors.toList());
        }
        return complaintRepository.countByTypeSince(LocalDateTime.now().minusDays(7)).stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("type", row[0]);
                    m.put("count", row[1]);
                    return m;
                }).collect(Collectors.toList());
    }
}
