package com.example.gym.backend.repository;

import com.example.gym.backend.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {

    Optional<Gym> findByGymCode(String gymCode);

    List<Gym> findByIsActiveTrue();

    @Query("SELECT g FROM Gym g WHERE g.isActive = true")
    List<Gym> findActiveGyms();
}
