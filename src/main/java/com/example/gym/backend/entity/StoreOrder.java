package com.example.gym.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "store_orders", indexes = {
    @Index(name = "idx_order_gym", columnList = "gym_id"),
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_date", columnList = "orderDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "transaction_id")
    private String transactionId;

    @Positive
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Positive
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Positive
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Positive
    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Positive
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderNumber == null || orderNumber.isEmpty()) {
            this.orderNumber = "ORD-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED,
        REFUNDED
    }

    // Helper method to calculate total
    public void calculateTotal() {
        if (items != null) {
            subtotal = items.stream()
                    .map(OrderItem::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        totalAmount = subtotal
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .add(shippingCost != null ? shippingCost : BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }
}
