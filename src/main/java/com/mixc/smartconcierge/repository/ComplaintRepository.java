package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    long countByStatus(Integer status);

    @Query("SELECT c FROM Complaint c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:start IS NULL OR c.createTime >= :start) AND " +
            "(:end IS NULL OR c.createTime <= :end) ORDER BY c.createTime DESC")
    Page<Complaint> search(@Param("status") Integer status,
                           @Param("type") String type,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end,
                           Pageable pageable);

    @Query("SELECT c.type, COUNT(c) FROM Complaint c WHERE c.createTime >= :since GROUP BY c.type ORDER BY COUNT(c) DESC")
    List<Object[]> countByTypeSince(@Param("since") LocalDateTime since);

    long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.createTime >= :since")
    long countSince(@Param("since") LocalDateTime since);
}
