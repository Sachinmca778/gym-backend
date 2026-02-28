package com.example.gym.backend.controller;

import com.example.gym.backend.dto.PaymentDto;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.repository.UserRepository;
import com.example.gym.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gym/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * Helper method to get the authenticated user's gym ID
     * Returns null if user is SUPER_USER (should see all data)
     */
    private Long getAuthenticatedUserGymId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return null;
        }
        
        // SUPER_USER should see all, return null to indicate no filter
        if (user.getRole() == User.UserRole.SUPER_USER) {
            return null;
        }
        
        // For ADMIN, RECEPTIONIST, etc., return their gym ID
        Gym gym = user.getGym();
        if (gym != null) {
            return gym.getId();
        }
        
        return null;
    }

    /**
     * Check if current user is SUPER_USER
     */
    private boolean isSuperUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        return user != null && user.getRole() == User.UserRole.SUPER_USER;
    }

    @PostMapping("/create_record")
    @PreAuthorize("hasAnyAuthority('ADMIN','RECEPTIONIST')")
    public ResponseEntity<PaymentDto> recordPayment(@RequestBody PaymentDto paymentDto) {
        log.info("Recording payment for User ID: {}", paymentDto.getUserId());
        PaymentDto payment = paymentService.recordPayment(paymentDto);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('SUPER_USER', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getPaymentSummary (
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Long gymId = getAuthenticatedUserGymId();
        
        Double currentMonthAmount;
        BigDecimal todayRevenue;
        List<PaymentDto> overDuePayments;
        BigDecimal totalOverdueAmount;
        BigDecimal pendingAmount;
        
        if (gymId != null) {
            // Admin/Receptionist - filter by gym
            currentMonthAmount = paymentService.getCurrentMonthTotalAmountByGymId(gymId);
            todayRevenue = paymentService.getTotalRevenueByDateAndGymId(gymId, date);
            overDuePayments = paymentService.getOverduePaymentsByGymId(gymId);
            pendingAmount = paymentService.getTotalPendingAmountByGymId(gymId);
        } else {
            // Super user - see all
            currentMonthAmount = paymentService.getCurrentMonthTotalAmount();
            todayRevenue = paymentService.getTotalRevenueByDate(date);
            overDuePayments = paymentService.getOverduePayments();
            pendingAmount = paymentService.getTotalPendingAmount();
        }
        
        totalOverdueAmount = overDuePayments.stream()
                .map(PaymentDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> payments = new HashMap<>();
        payments.put("currentMonthAmount", currentMonthAmount);
        payments.put("todayRevenue", todayRevenue);
        payments.put("totalOverdueAmount", totalOverdueAmount);
        payments.put("pendingAmount", pendingAmount);

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/all_payments")
    @PreAuthorize("hasAnyAuthority('SUPER_USER', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> findAllPayments(
            @RequestParam(required = false, defaultValue = "RECENT") String filter,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size) {
        Long gymId = getAuthenticatedUserGymId();
        
        List<PaymentDto> payments;
        long totalCount;
        
        if (gymId != null) {
            // Admin/Receptionist - filter by gym
            Map<String, Object> result = paymentService.findPaymentsByFilter(gymId, filter, page, size);
            payments = (List<PaymentDto>) result.get("payments");
            totalCount = (Long) result.get("totalCount");
        } else {
            // Super user - see all
            Map<String, Object> result = paymentService.findPaymentsByFilter(null, filter, page, size);
            payments = (List<PaymentDto>) result.get("payments");
            totalCount = (Long) result.get("totalCount");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", payments);
        response.put("totalElements", totalCount);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{userId}")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public ResponseEntity<List<PaymentDto>> getMemberPayments(@PathVariable Long userId) {
        log.info("Fetching payments for member ID: {}", userId);
        List<PaymentDto> payments = paymentService.getMemberPayments(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyAuthority('SUPER_USER', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<PaymentDto>> getOverduePayments() {
        log.info("Fetching overdue payments");
        Long gymId = getAuthenticatedUserGymId();
        List<PaymentDto> payments;
        
        if (gymId != null) {
            // Admin/Receptionist - filter by gym
            payments = paymentService.getOverduePaymentsByGymId(gymId);
        } else {
            // Super user - see all
            payments = paymentService.getOverduePayments();
        }
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/revenue/daily")
    @PreAuthorize("hasAnyAuthority('SUPER_USER', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<BigDecimal> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching daily revenue for date: {}", date);
        Long gymId = getAuthenticatedUserGymId();
        BigDecimal revenue;
        
        if (gymId != null) {
            // Admin/Receptionist - filter by gym
            revenue = paymentService.getTotalRevenueByDateAndGymId(gymId, date);
        } else {
            // Super user - see all
            revenue = paymentService.getTotalRevenueByDate(date);
        }
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/pending")
    @PreAuthorize("hasAnyAuthority('SUPER_USER', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<BigDecimal> getTotalPendingAmount() {
        log.info("Fetching total pending amount");
        Long gymId = getAuthenticatedUserGymId();
        BigDecimal pendingAmount;
        
        if (gymId != null) {
            // Admin/Receptionist - filter by gym
            pendingAmount = paymentService.getTotalPendingAmountByGymId(gymId);
        } else {
            // Super user - see all
            pendingAmount = paymentService.getTotalPendingAmount();
        }
        return ResponseEntity.ok(pendingAmount);
    }
}