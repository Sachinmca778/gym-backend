package com.example.gym.backend.service;

import com.example.gym.backend.dto.PaymentDto;
import com.example.gym.backend.entity.Member;
import com.example.gym.backend.entity.MemberMembership;
import com.example.gym.backend.entity.MembershipPlan;
import com.example.gym.backend.entity.Payment;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.GymRepository;
import com.example.gym.backend.repository.MemberRepository;
import com.example.gym.backend.repository.MemberMembershipRepository;
import com.example.gym.backend.repository.MembershipPlanRepository;
import com.example.gym.backend.repository.PaymentRepository;
import com.example.gym.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final MemberMembershipRepository membershipRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;

    public PaymentDto recordPayment(PaymentDto paymentDto) {
        log.info("Recording payment for User ID: {}", paymentDto.getUserId());

        User user = userRepository.findById(paymentDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + paymentDto.getUserId()));

        Payment payment = new Payment();
        payment.setUser(user);
        
        // Set gym from user's gym (or from DTO if provided)
        if (paymentDto.getGymId() != null) {
            Gym gym = gymRepository.findById(paymentDto.getGymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + paymentDto.getGymId()));
            payment.setGym(gym);
        } else if (user.getGym() != null) {
            payment.setGym(user.getGym());
        }
        
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setTransactionId(paymentDto.getTransactionId());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(java.time.LocalDateTime.now());
        payment.setDueDate(paymentDto.getDueDate());
        payment.setNotes(paymentDto.getNotes());


        // If membershipPlan ID is provided, link it (from MembershipPlan table)
        if (paymentDto.getMembershipPlanId() != null) {
            MembershipPlan membershipPlan = membershipPlanRepository.findById(paymentDto.getMembershipPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Membership Plan not found with ID: " + paymentDto.getMembershipPlanId()));
            payment.setMembershipPlan(membershipPlan);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment recorded successfully with ID: {}", savedPayment.getId());

        return convertToDto(savedPayment);
    }

    public Double getCurrentMonthTotalAmount() {
        LocalDateTime startDate = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = LocalDateTime.now();
        return paymentRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    public List<PaymentDto> getMemberPayments(Long userId) {
        log.info("Fetching payments for user ID: {}", userId);
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<PaymentDto> getOverduePayments() {
        log.info("Fetching overdue payments");
        List<Payment> payments = paymentRepository.findOverduePayments(LocalDate.now());
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<PaymentDto> findAllPayments() {
        List<Payment> payments = paymentRepository.findAllPayments();
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public BigDecimal getTotalRevenueByDate(LocalDate date) {
        return paymentRepository.getTotalRevenueByDate(date);
    }

    public BigDecimal getTotalPendingAmount() {
        return paymentRepository.getTotalPendingAmount();
    }

    // Gym-wise payment methods
    public List<PaymentDto> findPaymentsByGymId(Long gymId) {
        log.info("Fetching payments for gym ID: {}", gymId);
        List<Payment> payments = paymentRepository.findByGymId(gymId);
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<PaymentDto> getOverduePaymentsByGymId(Long gymId) {
        log.info("Fetching overdue payments for gym ID: {}", gymId);
        List<Payment> payments = paymentRepository.findOverduePaymentsByGymId(gymId, LocalDate.now());
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public BigDecimal getTotalRevenueByDateAndGymId(Long gymId, LocalDate date) {
        return paymentRepository.getTotalRevenueByDateAndGymId(gymId, date);
    }

    public BigDecimal getTotalPendingAmountByGymId(Long gymId) {
        return paymentRepository.getTotalPendingAmountByGymId(gymId);
    }

    public Double getCurrentMonthTotalAmountByGymId(Long gymId) {
        LocalDateTime startDate = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = LocalDateTime.now();
        return paymentRepository.getTotalRevenueByDateRangeAndGymId(gymId, startDate, endDate);
    }

    /**
     * Get payments with filters and pagination
     * Filter types: RECENT, TODAY_EXPIRES, UPCOMING_7_DAYS, OVERDUES
     */
    public Map<String, Object> findPaymentsByFilter(Long gymId, String filter, int page, int size) {
        log.info("Fetching payments with filter: {} for gymId: {}", filter, gymId);
        
        List<PaymentDto> allFilteredPayments;
        LocalDate today = LocalDate.now();
        
        switch (filter.toUpperCase()) {
            case "TODAY_EXPIRES":
                // Memberships expiring today
                allFilteredPayments = getTodayExpiringMemberships(gymId, today);
                break;
                
            case "UPCOMING_7_DAYS":
                // Memberships expiring in next 7 days (not including today)
                allFilteredPayments = getUpcomingExpiringMemberships(gymId, today);
                break;
                
            case "OVERDUES":
                // Memberships expired yesterday (one day ago)
                allFilteredPayments = getOverdueMemberships(gymId, today);
                break;
                
            case "RECENT":
            default:
                // Recent transactions - most recent payments first
                allFilteredPayments = getRecentPayments(gymId);
                break;
        }
        
        // Calculate pagination
        int start = page * size;
        int end = Math.min(start + size, allFilteredPayments.size());
        
        List<PaymentDto> pagedPayments;
        if (start < allFilteredPayments.size()) {
            pagedPayments = allFilteredPayments.subList(start, end);
        } else {
            pagedPayments = List.of();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("payments", pagedPayments);
        result.put("totalCount", (long) allFilteredPayments.size());
        
        return result;
    }
    
    private List<PaymentDto> getRecentPayments(Long gymId) {
        List<Payment> payments;
        if (gymId != null) {
            payments = paymentRepository.findByGymId(gymId);
        } else {
            payments = paymentRepository.findAllPayments();
        }
        
        // Sort by payment date descending (most recent first)
        return payments.stream()
            .sorted((p1, p2) -> {
                if (p1.getPaymentDate() == null && p2.getPaymentDate() == null) return 0;
                if (p1.getPaymentDate() == null) return 1;
                if (p2.getPaymentDate() == null) return -1;
                return p2.getPaymentDate().compareTo(p1.getPaymentDate());
            })
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private List<PaymentDto> getTodayExpiringMemberships(Long gymId, LocalDate today) {
        // Find memberships ending today
        List<MemberMembership> memberships;
        if (gymId != null) {
            memberships = membershipRepository.findByGymIdAndEndDate(gymId, today);
        } else {
            memberships = membershipRepository.findByEndDate(today);
        }
        
        return memberships.stream()
            .map(m -> {
                PaymentDto dto = new PaymentDto();
                dto.setId(m.getId());
                // Member has userId field directly
                dto.setUserId(m.getMember() != null ? m.getMember().getUserId() : null);
                dto.setAmount(m.getAmountPaid());
                dto.setDueDate(m.getEndDate());
                dto.setStatus(Payment.PaymentStatus.PENDING);
                dto.setNotes("Membership expires today - " + (m.getPlan() != null ? m.getPlan().getName() : ""));
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    private List<PaymentDto> getUpcomingExpiringMemberships(Long gymId, LocalDate today) {
        LocalDate nextWeek = today.plusDays(7);
        
        List<MemberMembership> memberships;
        if (gymId != null) {
            memberships = membershipRepository.findByGymIdAndEndDateBetween(gymId, today.plusDays(1), nextWeek);
        } else {
            memberships = membershipRepository.findByEndDateBetween(today.plusDays(1), nextWeek);
        }
        
        return memberships.stream()
            .map(m -> {
                PaymentDto dto = new PaymentDto();
                dto.setId(m.getId());
                // Member has userId field directly
                dto.setUserId(m.getMember() != null ? m.getMember().getUserId() : null);
                dto.setAmount(m.getAmountPaid());
                dto.setDueDate(m.getEndDate());
                dto.setStatus(Payment.PaymentStatus.PENDING);
                dto.setNotes("Membership expiring in " + java.time.temporal.ChronoUnit.DAYS.between(today, m.getEndDate()) + " days - " + 
                    (m.getPlan() != null ? m.getPlan().getName() : ""));
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    private List<PaymentDto> getOverdueMemberships(Long gymId, LocalDate today) {
        // Overdues = expired one day ago
        LocalDate yesterday = today.minusDays(1);
        
        List<MemberMembership> memberships;
        if (gymId != null) {
            memberships = membershipRepository.findByGymIdAndEndDate(gymId, yesterday);
        } else {
            memberships = membershipRepository.findByEndDate(yesterday);
        }
        
        return memberships.stream()
            .map(m -> {
                PaymentDto dto = new PaymentDto();
                dto.setId(m.getId());
                // Member has userId field directly
                dto.setUserId(m.getMember() != null ? m.getMember().getUserId() : null);
                dto.setAmount(m.getAmountPaid());
                dto.setDueDate(m.getEndDate());
                dto.setStatus(Payment.PaymentStatus.PENDING);
                dto.setNotes("Membership overdue - expired yesterday - " + (m.getPlan() != null ? m.getPlan().getName() : ""));
                return dto;
            })
            .collect(Collectors.toList());
    }

private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUser().getId());
        // dto.setMemberName(payment.getUser().getFirstName() + " " + payment.getUser().getLastName());
        // dto.setMembershipId(payment.getMembership() != null ? payment.getMembership().getId() : null);
        
        // Set membershipPlan info
        if (payment.getMembershipPlan() != null) {
            dto.setMembershipPlanId(payment.getMembershipPlan().getId());
        }
        
        // Set gym info
        if (payment.getGym() != null) {
            dto.setGymId(payment.getGym().getId());
            dto.setGymName(payment.getGym().getName());
        }
        
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionId(payment.getTransactionId());
        dto.setStatus(payment.getStatus());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setDueDate(payment.getDueDate());
        dto.setNotes(payment.getNotes());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}