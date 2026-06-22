package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_session")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String openid;

    @Column(length = 64)
    private String unionid;

    @Column(columnDefinition = "varbinary(100)")
    private byte[] phone;

    @Column(length = 50)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    private Integer gender;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        lastActive = now;
    }

    @PreUpdate
    public void preUpdate() {
        lastActive = LocalDateTime.now();
    }
}
