package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "daily_stats")
public class DailyStats {

    @Id
    @Column(name = "stat_date")
    private LocalDate statDate;

    @Column(name = "total_users")
    private Integer totalUsers = 0;

    @Column(name = "top_questions", columnDefinition = "json")
    private String topQuestions;

    @Column(name = "top_complaints", columnDefinition = "json")
    private String topComplaints;

    @Column(name = "satisfaction_index", precision = 5, scale = 2)
    private BigDecimal satisfactionIndex;

    @Column(name = "complaint_count")
    private Integer complaintCount = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}
