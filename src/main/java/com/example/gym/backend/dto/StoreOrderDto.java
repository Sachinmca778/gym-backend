package com.example.gym.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreOrderDto {
    private Long id;
    private String orderNumber;
    private Long gymId;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;
    private LocalDateTime orderDate;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String cancelReason;
    private List<OrderItemDto> items;
}
