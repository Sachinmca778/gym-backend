package com.example.gym.backend.controller;

import com.example.gym.backend.dto.MemberDto;
import com.example.gym.backend.dto.MemberSearchDto;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.entity.User.UserRole;
import com.example.gym.backend.repository.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    /**
     * Get current authenticated user from security context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        // Fallback: try to get user by username from authentication
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Get dashboard summary with role-based filtering using USER table:
     * - SUPER_USER: Returns overall counts across all gyms
     * - ADMIN, MANAGER, RECEPTIONIST: Returns counts filtered by their gym_id
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyAuthority('SUPER_USER', 'ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        User currentUser = getCurrentUser();

        long totalUsers;
        long activeUsers;
        Double totalPayments;
        long memberCount; // Users with MEMBER role
        long trainerCount; // Users with TRAINER role
        long staffCount; // Users with ADMIN, MANAGER, RECEPTIONIST roles

        // Check if user is SUPER_USER - return overall data
        if (currentUser != null && currentUser.getRole() == User.UserRole.SUPER_USER) {
            // SUPER_USER gets overall counts across all gyms
            totalUsers = userRepository.count();
            activeUsers = userRepository.findAllActiveUsers().size();
            totalPayments = paymentService.getCurrentMonthTotalAmount();
            if (totalPayments == null) totalPayments = 0.0;
            
            // Count users by role
            memberCount = userRepository.countActiveByRole(UserRole.MEMBER);
            trainerCount = userRepository.countActiveByRole(UserRole.TRAINER);
            staffCount = userRepository.countActiveByRole(UserRole.ADMIN) 
                        + userRepository.countActiveByRole(UserRole.MANAGER) 
                        + userRepository.countActiveByRole(UserRole.RECEPTIONIST);
        } else {
            // ADMIN, MANAGER, RECEPTIONIST get gym-specific data
            Long gymId = currentUser != null && currentUser.getGym() != null
                ? currentUser.getGym().getId()
                : null;

            if (gymId == null) {
                // If no gym assigned, return zeros
                totalUsers = 0;
                activeUsers = 0;
                totalPayments = 0.0;
                memberCount = 0;
                trainerCount = 0;
                staffCount = 0;
            } else {
                totalUsers = userRepository.countByGymId(gymId);
                activeUsers = userRepository.countActiveByGymId(gymId);
                totalPayments = paymentService.getCurrentMonthTotalAmountByGymId(gymId);
                if (totalPayments == null) totalPayments = 0.0;
                
                // Count users by role for this gym
                memberCount = userRepository.countByGymIdAndRole(gymId, UserRole.MEMBER);
                trainerCount = userRepository.countByGymIdAndRole(gymId, UserRole.TRAINER);
                staffCount = userRepository.countByGymIdAndRole(gymId, UserRole.ADMIN)
                           + userRepository.countByGymIdAndRole(gymId, UserRole.MANAGER)
                           + userRepository.countByGymIdAndRole(gymId, UserRole.RECEPTIONIST);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", totalUsers);
        response.put("activeUsers", activeUsers);
        response.put("totalPaymentsCurrentMonth", totalPayments);
        response.put("memberCount", memberCount);
        response.put("trainerCount", trainerCount);
        response.put("staffCount", staffCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
        log.info("Creating new member: {}", memberDto.getFirstName() + " " + memberDto.getLastName());
        MemberDto createdMember = memberService.createMember(memberDto);
        return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('SUPER_USER','ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        List<MemberDto> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
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