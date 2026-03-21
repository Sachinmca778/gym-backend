package com.example.gym.backend.repository;

import com.example.gym.backend.entity.StoreOrder;
import com.example.gym.backend.entity.StoreOrder.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {

    @Query("SELECT o FROM StoreOrder o WHERE o.gym.id = :gymId ORDER BY o.orderDate DESC")
    Page<StoreOrder> findAllByGym(@Param("gymId") Long gymId, Pageable pageable);

    @Query("SELECT o FROM StoreOrder o WHERE o.gym.id = :gymId AND o.user.id = :userId ORDER BY o.orderDate DESC")
    Page<StoreOrder> findByGymAndUser(@Param("gymId") Long gymId, 
                                       @Param("userId") Long userId, 
                                       Pageable pageable);

    @Query("SELECT o FROM StoreOrder o WHERE o.gym.id = :gymId AND o.status = :status ORDER BY o.orderDate DESC")
    Page<StoreOrder> findByGymAndStatus(@Param("gymId") Long gymId, 
                                         @Param("status") OrderStatus status, 
                                         Pageable pageable);

    @Query("SELECT o FROM StoreOrder o WHERE o.gym.id = :gymId AND o.orderDate >= :startDate AND o.orderDate < :endDate ORDER BY o.orderDate DESC")
    Page<StoreOrder> findByGymAndDateRange(@Param("gymId") Long gymId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    @Query("SELECT o FROM StoreOrder o WHERE o.gym.id = :gymId AND o.status = :status")
    List<StoreOrder> findPendingOrders(@Param("gymId") Long gymId, @Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM StoreOrder o WHERE o.gym.id = :gymId AND o.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED') AND o.orderDate >= :startDate AND o.orderDate < :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("gymId") Long gymId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM StoreOrder o WHERE o.gym.id = :gymId AND o.orderDate >= :startDate AND o.orderDate < :endDate")
    long countOrdersByDateRange(@Param("gymId") Long gymId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(o.totalAmount) FROM StoreOrder o WHERE o.gym.id = :gymId AND o.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED') AND o.orderDate >= :startDate AND o.orderDate < :endDate")
    BigDecimal getAverageOrderValue(@Param("gymId") Long gymId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    StoreOrder findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}
