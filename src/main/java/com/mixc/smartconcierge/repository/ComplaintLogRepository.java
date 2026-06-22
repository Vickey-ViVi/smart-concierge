package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.ComplaintLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintLogRepository extends JpaRepository<ComplaintLog, Long> {

    List<ComplaintLog> findByComplaintIdOrderByCreateTimeAsc(Long complaintId);
}
