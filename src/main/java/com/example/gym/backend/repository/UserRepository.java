package com.example.gym.backend.repository;

import com.example.gym.backend.entity.User;
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
    List<User> findActiveUsersByRole(@Param("role") User.UserRole role);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND u.isActive = true")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}
