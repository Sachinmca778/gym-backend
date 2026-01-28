package com.example.gym.backend.repository;

import com.example.gym.backend.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    // Find all active trainers
    List<Trainer> findByIsActiveTrue();

    // Find active trainers by specialization
    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND LOWER(t.specialization) = LOWER(:specialization)")
    List<Trainer> findActiveTrainersBySpecialization(@Param("specialization") String specialization);

    // Find top rated trainers
    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND t.rating >= :minRating ORDER BY t.rating DESC")
    List<Trainer> findTopRatedTrainers(@Param("minRating") BigDecimal minRating);

    // Find trainers by email (for uniqueness check)
    boolean existsByEmail(String email);

    // Find trainers by specialization (case insensitive)
    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND LOWER(t.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    List<Trainer> findBySpecializationContainingIgnoreCase(@Param("specialization") String specialization);

    // Validation methods for unique constraints
    boolean existsByUserId(Long userId);
    Optional<Trainer> findByUserId(Long userId);
}
