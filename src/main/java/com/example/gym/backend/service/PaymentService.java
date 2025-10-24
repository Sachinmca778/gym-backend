package com.example.gym.backend.service;

import com.example.gym.backend.dto.PaymentDto;
import com.example.gym.backend.entity.Member;
import com.example.gym.backend.entity.MemberMembership;
import com.example.gym.backend.entity.Payment;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.MemberRepository;
import com.example.gym.backend.repository.MemberMembershipRepository;
import com.example.gym.backend.repository.PaymentRepository;
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

    public PaymentDto recordPayment(PaymentDto paymentDto) {
        log.info("Recording payment for member ID: {}", paymentDto.getMemberId());

        Member member = memberRepository.findById(paymentDto.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + paymentDto.getMemberId()));

        Payment payment = new Payment();
        payment.setMember(member);
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

    public List<PaymentDto> getMemberPayments(Long memberId) {
        log.info("Fetching payments for member ID: {}", memberId);
        List<Payment> payments = paymentRepository.findByMemberId(memberId);
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

    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setMemberId(payment.getMember().getId());
        dto.setMemberName(payment.getMember().getFirstName() + " " + payment.getMember().getLastName());
        dto.setMembershipId(payment.getMembership() != null ? payment.getMembership().getId() : null);
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