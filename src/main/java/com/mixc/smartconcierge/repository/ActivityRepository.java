package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByStatusAndEndDateBefore(Integer status, LocalDateTime endDate);

    Page<Activity> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
