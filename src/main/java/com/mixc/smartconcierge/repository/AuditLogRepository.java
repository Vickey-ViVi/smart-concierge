package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:username IS NULL OR a.username = :username) AND " +
            "(:start IS NULL OR a.createTime >= :start) AND " +
            "(:end IS NULL OR a.createTime <= :end) ORDER BY a.createTime DESC")
    Page<AuditLog> search(@Param("username") String username,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end,
                          Pageable pageable);
}
