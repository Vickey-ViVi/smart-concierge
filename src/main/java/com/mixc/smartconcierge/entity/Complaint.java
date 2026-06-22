package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaint")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String openid;

    @Column(columnDefinition = "varbinary(100)")
    private byte[] phone;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "image_urls", columnDefinition = "json")
    private String imageUrls;

    @Column(columnDefinition = "tinyint default 0")
    private Integer status = 0;

    @Column(length = 50)
    private String assignee;

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
