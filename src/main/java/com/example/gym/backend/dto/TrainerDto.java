package com.example.gym.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TrainerDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialization;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private String certifications;
    private String bio;
    private String schedule;
    private String location;
    private BigDecimal rating;
    private Integer totalRatings;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}