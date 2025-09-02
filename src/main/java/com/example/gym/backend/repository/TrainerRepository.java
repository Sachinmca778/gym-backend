package com.example.gym.backend.repository;

import com.example.gym.backend.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByEmail(String email);
    List<Trainer> findByIsActiveTrue();
    List<Trainer> findBySpecialization(String specialization);

    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND t.specialization = :specialization")
    List<Trainer> findActiveTrainersBySpecialization(@Param("specialization") String specialization);

    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND t.rating >= :minRating ORDER BY t.rating DESC")
    List<Trainer> findTopRatedTrainers(@Param("minRating") Double minRating);

    @Query("SELECT t FROM Trainer t WHERE t.isActive = true AND t.hourlyRate <= :maxRate")
    List<Trainer> findTrainersByMaxRate(@Param("maxRate") Double maxRate);
}