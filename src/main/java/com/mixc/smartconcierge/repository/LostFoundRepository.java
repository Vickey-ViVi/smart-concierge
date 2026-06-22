package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.LostFound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LostFoundRepository extends JpaRepository<LostFound, Long> {

    @Query("SELECT l FROM LostFound l WHERE l.status = 0 AND l.itemName LIKE %:keyword%")
    List<LostFound> matchByKeyword(@Param("keyword") String keyword);
}
