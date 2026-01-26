package com.example.gym.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberMembershipDto {

    private Long id;

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotBlank(message = "Start date is required")
    private String startDate;

    @NotBlank(message = "End date is required")
    private String endDate;

    @NotNull(message = "Amount paid is required")
    @Positive(message = "Amount paid must be positive")
    private BigDecimal amountPaid;

    private Long gymId;

    private String status;
    private boolean autoRenewal;
    private LocalDateTime createdAt;
}
