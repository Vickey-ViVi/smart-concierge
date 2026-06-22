package com.mixc.smartconcierge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "node_edges")
public class NodeEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_node", nullable = false)
    private Long fromNode;

    @Column(name = "to_node", nullable = false)
    private Long toNode;

    @Column(nullable = false)
    private Integer distance;

    @Column(name = "path_type", length = 20)
    private String pathType = "walk";

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }
}
