package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
}
