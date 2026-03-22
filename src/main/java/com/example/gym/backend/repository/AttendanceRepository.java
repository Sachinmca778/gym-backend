package com.example.gym.backend.repository;

import com.example.gym.backend.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Get all attendance for a specific date
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        ORDER BY a.checkIn DESC
    """)
    Page<Attendance> findAllByDate(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            Pageable pageable);

    // Get all attendance for a specific gym and date
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.gym.id = :gymId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        ORDER BY a.checkIn DESC
    """)
    Page<Attendance> findAllByGymAndDate(
            @Param("gymId") Long gymId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            Pageable pageable);

    // Get currently present members (checked in but not checked out)
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.gym.id = :gymId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        AND a.checkOut IS NULL
        ORDER BY a.checkIn DESC
    """)
    List<Attendance> findCurrentlyPresent(
            @Param("gymId") Long gymId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Count today's attendance
    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.gym.id = :gymId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
    """)
    long countTodayAttendance(
            @Param("gymId") Long gymId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Count currently present
    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.gym.id = :gymId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        AND a.checkOut IS NULL
    """)
    long countCurrentlyPresent(
            @Param("gymId") Long gymId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Get attendance for date range
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.gym.id = :gymId
        AND a.checkIn >= :startDate
        AND a.checkIn < :endDate
        ORDER BY a.checkIn DESC
    """)
    Page<Attendance> findByDateRange(
            @Param("gymId") Long gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Get average duration for a date
    @Query("""
        SELECT AVG(a.durationMinutes) FROM Attendance a
        WHERE a.gym.id = :gymId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        AND a.durationMinutes IS NOT NULL
    """)
    Double getAverageDuration(
            @Param("gymId") Long gymId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Get peak hours (group by hour)
    @Query("""
        SELECT HOUR(a.checkIn) as hour, COUNT(a) as count
        FROM Attendance a
        WHERE a.gym.id = :gymId
        AND a.checkIn >= :startOfDay
        AND a.checkIn < :endOfDay
        GROUP BY HOUR(a.checkIn)
        ORDER BY count DESC
    """)
    List<Object[]> getPeakHours(
            @Param("gymId") Long gymId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}
