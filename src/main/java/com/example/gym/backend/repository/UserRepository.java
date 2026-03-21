package com.example.gym.backend.repository;

import com.example.gym.backend.entity.User;
import com.example.gym.backend.entity.User.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    Page<User> findActiveUsersByRole(@Param("role") User.UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActiveUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND u.role = 'MEMBER'")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Gym-based queries for dashboard
    @Query("SELECT u FROM User u WHERE u.gym.id = :gymId")
    Page<User> findByGymId(@Param("gymId") Long gymId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.gym.id = :gymId")
    long countByGymId(@Param("gymId") Long gymId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.gym.id = :gymId AND u.isActive = true")
    long countActiveByGymId(@Param("gymId") Long gymId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.gym.id = :gymId AND u.role = :role AND u.isActive = true")
    long countByGymIdAndRole(@Param("gymId") Long gymId, @Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveByRole(@Param("role") UserRole role);

    // Get all users (for admin to select when creating member)
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActive(Pageable pageable);

    // Get users by gym
    @Query("SELECT u FROM User u WHERE u.gym.id = :gymId AND u.isActive = true")
    Page<User> findActiveByGymId(@Param("gymId") Long gymId, Pageable pageable);
}

