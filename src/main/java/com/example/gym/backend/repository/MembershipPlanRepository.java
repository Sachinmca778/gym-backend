package com.example.gym.backend.repository;


import com.example.gym.backend.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    List<MembershipPlan> findByIsActiveTrue();

    @Query("SELECT p FROM MembershipPlan p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice")
    List<MembershipPlan> findActivePlansByPriceRange(@Param("minPrice") Double minPrice,
                                                     @Param("maxPrice") Double maxPrice);

    @Query("SELECT p FROM MembershipPlan p WHERE p.isActive = true AND p.durationMonths = :duration")
    List<MembershipPlan> findActivePlansByDuration(@Param("duration") Integer duration);
}