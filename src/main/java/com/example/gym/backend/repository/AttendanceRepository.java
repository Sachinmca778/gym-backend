package com.example.gym.backend.repository;


import com.example.gym.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByMemberId(Long memberId);

    @Query("SELECT a FROM Attendance a WHERE a.member.id = :memberId AND DATE(a.checkIn) = :date")
    List<Attendance> findByMemberIdAndDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE DATE(a.checkIn) = :date")
    List<Attendance> findByDate(@Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.member.id = :memberId AND a.checkIn BETWEEN :startDate AND :endDate")
    List<Attendance> findByMemberIdAndDateRange(@Param("memberId") Long memberId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE DATE(a.checkIn) = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.member.id = :memberId AND DATE(a.checkIn) = :date")
    long countByMemberIdAndDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    @Query("SELECT AVG(a.durationMinutes) FROM Attendance a WHERE DATE(a.checkIn) = :date")
    Double findAvgDurationByDate(@Param("date") LocalDate date);
}