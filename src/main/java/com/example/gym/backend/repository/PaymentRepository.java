package com.example.gym.backend.repository;


import com.example.gym.backend.entity.Payment;
import com.example.gym.backend.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMemberId(Long memberId);
    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.member.id = :memberId AND p.status = :status")
    List<Payment> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.dueDate <= :dueDate AND p.status = 'PENDING'")
    List<Payment> findOverduePayments(@Param("dueDate") LocalDate dueDate);

    @Query(
            value = """
            SELECT *
            FROM (
                SELECT 
                    p.*, 
                    ROW_NUMBER() OVER (PARTITION BY p.status ORDER BY p.id DESC) AS rn
                FROM payments p
                WHERE p.status IN ('COMPLETED', 'FAILED', 'PENDING')
            ) AS ranked
            ORDER BY 
                ranked.rn,
                FIELD(ranked.status, 'COMPLETED', 'FAILED', 'PENDING')
            LIMIT 10
            """,
            nativeQuery = true
    )
    List<Payment> findAllPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND DATE(p.paymentDate) = :date")
    BigDecimal getTotalRevenueByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PENDING'")
    BigDecimal getTotalPendingAmount();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}