package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.ProposalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProposalRecordRepository extends JpaRepository<ProposalRecord, Long> {

    @Query("SELECT r FROM ProposalRecord r WHERE " +
            "(:brandName IS NULL OR r.brandName LIKE %:brandName%) ORDER BY r.createTime DESC")
    Page<ProposalRecord> search(@Param("brandName") String brandName, Pageable pageable);
}
