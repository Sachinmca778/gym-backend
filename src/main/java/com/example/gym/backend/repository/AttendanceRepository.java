package com.example.gym.backend.repository;

import com.example.gym.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.user.id = :userId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        AND a.checkOut IS NULL
    """)
    Optional<Attendance> findOpenAttendance(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.user.id = :userId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        AND a.checkOut IS NOT NULL
        ORDER BY a.checkIn DESC
    """)
    List<Attendance> findCompletedAttendanceByUserAndDate(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
            

}
