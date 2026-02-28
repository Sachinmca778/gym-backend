package com.example.gym.backend.dto;


import lombok.Data;

import com.example.gym.backend.entity.Payment.PaymentMethod;
import com.example.gym.backend.entity.Payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentDto {

    private Long id;
    private Long userId;
    private String memberName;
    private String memberPhone;
    private String memberEmail;
    // private String memberName;
    // private Long membershipId;
    private Long membershipPlanId;
    private String membershipPlanName;
    private Long gymId;
    private String gymName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private LocalDate dueDate;
    private String notes;
    private LocalDateTime createdAt;
}
