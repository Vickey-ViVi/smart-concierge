package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shop")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String floor;

    @Column(length = 50)
    private String category;

    @Column(name = "sub_category", length = 50)
    private String subCategory;

    @Column(columnDefinition = "json")
    private String tags;

    @Column(name = "avg_price")
    private Integer avgPrice;

    @Column(name = "discount_info", columnDefinition = "text")
    private String discountInfo;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(columnDefinition = "tinyint default 1")
    private Integer status = 1;

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
