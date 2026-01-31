package com.example.gym.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttendanceDto {

    private Long id;
    private Long userId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer durationMinutes;
    private String method;
    private LocalDateTime createdAt;
}