package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyStatsRepository extends JpaRepository<DailyStats, LocalDate> {

    List<DailyStats> findByStatDateBetweenOrderByStatDateAsc(LocalDate start, LocalDate end);
}
