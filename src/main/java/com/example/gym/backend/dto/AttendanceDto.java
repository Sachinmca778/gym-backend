package com.example.gym.backend.dto;


import lombok.Data;

import com.example.gym.backend.entity.Attendance.CheckInMethod;
import java.time.LocalDateTime;

@Data
public class AttendanceDto {

    private Long id;
    private Long memberId;
    private String memberName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer durationMinutes;
    private CheckInMethod method;
    private String notes;
    private LocalDateTime createdAt;
}