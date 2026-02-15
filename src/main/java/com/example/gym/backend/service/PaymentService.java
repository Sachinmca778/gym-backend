package com.example.gym.backend.service;

import com.example.gym.backend.dto.PaymentDto;
import com.example.gym.backend.entity.Member;
import com.example.gym.backend.entity.MemberMembership;
import com.example.gym.backend.entity.Payment;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.GymRepository;
import com.example.gym.backend.repository.MemberRepository;
import com.example.gym.backend.repository.MemberMembershipRepository;
import com.example.gym.backend.repository.PaymentRepository;
import com.example.gym.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final MemberMembershipRepository membershipRepository;
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

        // If membership ID is provided, link it
        if (paymentDto.getMembershipId() != null) {
            MemberMembership membership = membershipRepository.findById(paymentDto.getMembershipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Membership not found with ID: " + paymentDto.getMembershipId()));
            payment.setMembership(membership);
            // Also set gym from membership if not already set
            if (payment.getGym() == null && membership.getGym() != null) {
                payment.setGym(membership.getGym());
            }
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

private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUser().getId());
        dto.setMemberName(payment.getUser().getFirstName() + " " + payment.getUser().getLastName());
        dto.setMembershipId(payment.getMembership() != null ? payment.getMembership().getId() : null);
        
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