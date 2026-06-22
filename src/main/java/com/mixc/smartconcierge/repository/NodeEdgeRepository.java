package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.NodeEdge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeEdgeRepository extends JpaRepository<NodeEdge, Long> {

    List<NodeEdge> findByFromNodeOrToNode(Long fromNode, Long toNode);
}
