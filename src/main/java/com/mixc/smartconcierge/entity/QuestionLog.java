package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "question_log")
public class QuestionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String openid;

    @Column(name = "question_text", columnDefinition = "text", nullable = false)
    private String questionText;

    @Column(length = 20)
    private String intent;

    @Column(name = "answer_text", columnDefinition = "text")
    private String answerText;

    @Column(name = "recommend_detail", columnDefinition = "json")
    private String recommendDetail;

    @Column(name = "use_deepseek")
    private Integer useDeepseek = 0;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }
}
