package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
}
