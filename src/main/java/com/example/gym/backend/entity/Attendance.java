package com.example.gym.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "attendance", indexes = {
    @Index(name = "idx_attendance_user", columnList = "user_id"),
    @Index(name = "idx_attendance_gym", columnList = "gym_id"),
    @Index(name = "idx_attendance_checkin", columnList = "check_in"),
    @Index(name = "idx_attendance_checkout", columnList = "check_out")
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private CheckInMethod method;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum CheckInMethod {
        MANUAL, QR
    }
}