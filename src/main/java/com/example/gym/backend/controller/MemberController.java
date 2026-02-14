package com.example.gym.backend.controller;

import com.example.gym.backend.dto.MemberDto;
import com.example.gym.backend.dto.MemberSearchDto;
import com.example.gym.backend.service.MemberService;
import com.example.gym.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gym/members")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MemberController {

    private final MemberService memberService;
    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
        log.info("Creating new member: {}", memberDto.getFirstName() + " " + memberDto.getLastName());
        MemberDto createdMember = memberService.createMember(memberDto);
        return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        List<MemberDto> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        List<MemberDto> members = memberService.getAllMembers();
        long totalMembers = members.size();
        long activeMembers = memberService.getActiveMembersCount();
        Double totalPayments = paymentService.getCurrentMonthTotalAmount();
        if (totalPayments == null) totalPayments = 0.0;
        List<MemberDto> expiringMembers = memberService.getMembersWithExpiringMemberships(7);
        long expiringMembersCount = expiringMembers.size();
        Map<String, Object> response = new HashMap<>();
        response.put("totalMembers", totalMembers);
        response.put("activeMembers", activeMembers);
        response.put("totalPaymentsCurrentMonth", totalPayments);
        response.put("expiringMembersCount", expiringMembersCount);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long id) {
        log.info("Fetching member with ID: {}", id);
        MemberDto member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/code/{memberCode}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<MemberDto> getMemberByCode(@PathVariable String memberCode) {
        log.info("Fetching member with code: {}", memberCode);
        MemberDto member = memberService.getMemberByCode(memberCode);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/search")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<Page<MemberDto>> searchMembers(
            @ModelAttribute MemberSearchDto searchDto,
            Pageable pageable) {
        log.info("Searching members with criteria: {}", searchDto);
        Page<MemberDto> members = memberService.searchMembers(searchDto, pageable);
        return ResponseEntity.ok(members);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<MemberDto> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberDto memberDto) {
        log.info("Updating member with ID: {}", id);
        MemberDto updatedMember = memberService.updateMember(id, memberDto);
        return ResponseEntity.ok(updatedMember);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.info("Deleting member with ID: {}", id);
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<MemberDto>> getExpiringMemberships(
            @RequestParam(defaultValue = "7") int daysBeforeExpiry) {
        log.info("Fetching members with expiring memberships in {} days", daysBeforeExpiry);
        List<MemberDto> members = memberService.getMembersWithExpiringMemberships(daysBeforeExpiry);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> getActiveMembersCount() {
        log.info("Fetching active members count");
        long count = memberService.getActiveMembersCount();
        return ResponseEntity.ok(count);
    }
}