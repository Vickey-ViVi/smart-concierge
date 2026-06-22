package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.QuestionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuestionLogRepository extends JpaRepository<QuestionLog, Long> {

    @Query("SELECT q.intent, COUNT(q) FROM QuestionLog q WHERE q.createTime >= :since AND q.intent IS NOT NULL GROUP BY q.intent ORDER BY COUNT(q) DESC")
    List<Object[]> countByIntentSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT q.openid) FROM QuestionLog q WHERE q.createTime >= :start AND q.createTime < :end")
    long countDistinctOpenidBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT q FROM QuestionLog q WHERE " +
            "(:intent IS NULL OR q.intent = :intent) AND " +
            "(:start IS NULL OR q.createTime >= :start) AND " +
            "(:end IS NULL OR q.createTime <= :end) AND " +
            "q.recommendDetail IS NOT NULL ORDER BY q.createTime DESC")
    Page<QuestionLog> search(@Param("intent") String intent,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             Pageable pageable);

    @Query("SELECT COUNT(q) FROM QuestionLog q WHERE q.useDeepseek = 1 AND q.createTime >= :start AND q.createTime < :end")
    long countDeepseekSuccessBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(q) FROM QuestionLog q WHERE q.createTime >= :start AND q.createTime < :end")
    long countBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(q.responseTimeMs) FROM QuestionLog q WHERE q.useDeepseek = 1 AND q.createTime >= :start AND q.createTime < :end")
    Double avgDeepseekLatencyBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
