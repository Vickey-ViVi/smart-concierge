package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    Optional<Proposal> findByBrandName(String brandName);

    List<Proposal> findTop10ByOrderByCountDesc();

    @Query("SELECT p FROM Proposal p WHERE p.count >= COALESCE(p.threshold, :globalThreshold) ORDER BY p.count DESC")
    List<Proposal> findOverThreshold(@org.springframework.data.repository.query.Param("globalThreshold") int globalThreshold);
}
