package com.example.gym.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for member search and filtering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchDto {

    /**
     * Search term for name, email, or phone
     */
    private String searchTerm;

    /**
     * Member status filter
     */
    private String status;

    /**
     * Gender filter
     */
    private String gender;

    /**
     * City filter for address
     */
    private String city;

    /**
     * State filter for address
     */
    private String state;

    /**
     * Join date from filter
     */
    private LocalDate joinDateFrom;

    /**
     * Join date to filter
     */
    private LocalDate joinDateTo;

    /**
     * Age range filter - minimum age
     */
    private Integer minAge;

    /**
     * Age range filter - maximum age
     */
    private Integer maxAge;

    /**
     * Membership plan filter
     */
    private Long membershipPlanId;

    /**
     * Trainer assignment filter
     */
    private Long trainerId;

    /**
     * Active membership filter
     */
    private Boolean hasActiveMembership;

    /**
     * Expiring membership filter (within days)
     */
    private Integer expiringWithinDays;

    /**
     * Payment status filter
     */
    private String paymentStatus;
}