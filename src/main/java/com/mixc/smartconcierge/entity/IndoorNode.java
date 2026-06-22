package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "indoor_nodes")
public class IndoorNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    @Column(name = "node_type", length = 20)
    private String nodeType = "shop";

    @Column(nullable = false, length = 10)
    private String floor;

    @Column(name = "x_coord", nullable = false, precision = 10, scale = 2)
    private BigDecimal xCoord;

    @Column(name = "y_coord", nullable = false, precision = 10, scale = 2)
    private BigDecimal yCoord;

    @Column(name = "is_accessible")
    private Integer isAccessible = 1;

    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }
}
