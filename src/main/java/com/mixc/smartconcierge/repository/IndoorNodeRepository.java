package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.IndoorNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IndoorNodeRepository extends JpaRepository<IndoorNode, Long> {

    Optional<IndoorNode> findByShopId(Long shopId);

    List<IndoorNode> findByShopIdIn(List<Long> shopIds);
}
