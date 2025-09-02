package com.example.gym.backend.controller;

import com.example.gym.backend.dto.PaymentDto;
import com.example.gym.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<PaymentDto> recordPayment(@RequestBody PaymentDto paymentDto) {
        log.info("Recording payment for member ID: {}", paymentDto.getMemberId());
        PaymentDto payment = paymentService.recordPayment(paymentDto);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<PaymentDto>> getMemberPayments(@PathVariable Long memberId) {
        log.info("Fetching payments for member ID: {}", memberId);
        List<PaymentDto> payments = paymentService.getMemberPayments(memberId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<PaymentDto>> getOverduePayments() {
        log.info("Fetching overdue payments");
        List<PaymentDto> payments = paymentService.getOverduePayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/revenue/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BigDecimal> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching daily revenue for date: {}", date);
        BigDecimal revenue = paymentService.getTotalRevenueByDate(date);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BigDecimal> getTotalPendingAmount() {
        log.info("Fetching total pending amount");
        BigDecimal pendingAmount = paymentService.getTotalPendingAmount();
        return ResponseEntity.ok(pendingAmount);
    }
}