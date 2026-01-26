package com.example.gym.backend.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class GymDto {

    private Long id;

    @NotBlank(message = "Gym code is required")
    private String gymCode;

    @NotBlank(message = "Gym name is required")
    private String name;

    private String email;

    private String phone;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
