package com.example.gym.backend.controller;

import com.example.gym.backend.dto.MemberMembershipDto;
import com.example.gym.backend.service.MemberMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gym/memberships")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MemberMembershipController {

    private final MemberMembershipService membershipService;

    @PostMapping("/create")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<MemberMembershipDto> createMembership(@Valid @RequestBody MemberMembershipDto dto) {
        log.info("Creating new membership");
        MemberMembershipDto createdMembership = membershipService.createMembership(dto);
        return new ResponseEntity<>(createdMembership, HttpStatus.CREATED);
    }

    @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<MemberMembershipDto>> getAllMemberships() {
        log.info("Fetching all memberships");
        List<MemberMembershipDto> memberships = membershipService.getAllMemberships();
        return ResponseEntity.ok(memberships);
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<MemberMembershipDto> getMembershipById(@PathVariable Long id) {
        log.info("Fetching membership with ID: {}", id);
        MemberMembershipDto membership = membershipService.getMembershipById(id);
        return ResponseEntity.ok(membership);
    }

    @GetMapping("/member/{memberId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<MemberMembershipDto>> getMembershipsByMemberId(@PathVariable Long memberId) {
        log.info("Fetching memberships for member ID: {}", memberId);
        List<MemberMembershipDto> memberships = membershipService.getMembershipsByMemberId(memberId);
        return ResponseEntity.ok(memberships);
    }
}
