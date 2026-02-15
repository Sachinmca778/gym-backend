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

    List<Payment> findByUserId(Long userId);
    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);

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

    // Gym-wise payment queries
    @Query("SELECT p FROM Payment p WHERE p.gym.id = :gymId")
    List<Payment> findByGymId(@Param("gymId") Long gymId);

    @Query("SELECT p FROM Payment p WHERE p.gym.id = :gymId AND p.status = :status")
    List<Payment> findByGymIdAndStatus(@Param("gymId") Long gymId, @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.gym.id = :gymId AND p.dueDate <= :dueDate AND p.status = 'PENDING'")
    List<Payment> findOverduePaymentsByGymId(@Param("gymId") Long gymId, @Param("dueDate") LocalDate dueDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.gym.id = :gymId AND p.status = 'COMPLETED' AND DATE(p.paymentDate) = :date")
    BigDecimal getTotalRevenueByDateAndGymId(@Param("gymId") Long gymId, @Param("date") LocalDate date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.gym.id = :gymId AND p.status = 'PENDING'")
    BigDecimal getTotalPendingAmountByGymId(@Param("gymId") Long gymId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.gym.id = :gymId AND p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByDateRangeAndGymId(@Param("gymId") Long gymId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
