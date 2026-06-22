package com.mixc.smartconcierge.repository;

import com.mixc.smartconcierge.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByStatusOrderByLikeCountDesc(Integer status);

    @Query("SELECT s FROM Shop s WHERE s.status = 1 AND " +
            "(:category IS NULL OR s.category = :category) AND " +
            "(:keyword IS NULL OR s.name LIKE %:keyword% OR s.tags LIKE %:keyword%)")
    List<Shop> searchActive(@Param("category") String category, @Param("keyword") String keyword);

    Page<Shop> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
