package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "proposal")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_name", nullable = false, unique = true, length = 100)
    private String brandName;

    @Column(columnDefinition = "int default 1")
    private Integer count = 1;

    @Column(columnDefinition = "int default 100")
    private Integer threshold = 100;

    @Column(columnDefinition = "tinyint default 0")
    private Integer notified = 0;

    @Column(name = "last_propose_time")
    private LocalDateTime lastProposeTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
        lastProposeTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}
