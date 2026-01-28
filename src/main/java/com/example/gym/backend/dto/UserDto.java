package com.example.gym.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String password; // For creation only, not returned in responses
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private boolean isActive;
    private Long gymId; // NULL for ADMIN, NOT NULL for other roles
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

