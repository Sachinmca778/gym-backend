package com.example.gym.backend.repository;

import com.example.gym.backend.entity.StoreCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {

    @Query("SELECT c FROM StoreCategory c WHERE c.gym.id = :gymId AND c.isActive = true ORDER BY c.sortOrder")
    List<StoreCategory> findActiveCategoriesByGym(@Param("gymId") Long gymId);

    @Query("SELECT c FROM StoreCategory c WHERE c.gym.id = :gymId ORDER BY c.sortOrder")
    Page<StoreCategory> findAllByGym(@Param("gymId") Long gymId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM StoreCategory c WHERE c.gym.id = :gymId AND c.isActive = true")
    long countActiveCategories(@Param("gymId") Long gymId);

    List<StoreCategory> findByGymIdAndIsActiveTrue(Long gymId);
}
