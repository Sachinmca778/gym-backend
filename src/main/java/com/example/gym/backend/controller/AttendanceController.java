package com.example.gym.backend.controller;

import com.example.gym.backend.dto.AttendanceDto;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.repository.UserRepository;
import com.example.gym.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gyms/{gymId}/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @PostMapping("/check-in/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<AttendanceDto> checkIn(
            @PathVariable Long gymId,
            @PathVariable Long userId,
            @RequestBody AttendanceDto dto
    ) {
        log.info("Checking in user {} at gym {}", userId, gymId);
        AttendanceDto result = attendanceService.checkIn(gymId, userId, dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/check-out/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<AttendanceDto> checkOut(
            @PathVariable Long gymId,
            @PathVariable Long userId
    ) {
        log.info("Checking out user {} from gym {}", userId, gymId);
        AttendanceDto result = attendanceService.checkOut(gymId, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/current/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<AttendanceDto> getCurrentAttendance(
            @PathVariable Long gymId,
            @PathVariable Long userId
    ) {
        AttendanceDto result = attendanceService.getCurrentAttendance(gymId, userId);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/today/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<AttendanceDto>> getTodayAttendance(
            @PathVariable Long gymId,
            @PathVariable Long userId
    ) {
        List<AttendanceDto> result = attendanceService.getTodayCompletedAttendance(gymId, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today/list")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Page<AttendanceDto>> getTodayAttendanceList(
            @PathVariable Long gymId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkIn"));
        Page<AttendanceDto> result = attendanceService.getTodayAttendance(gymId, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/currently-present")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<AttendanceDto>> getCurrentlyPresent(
            @PathVariable Long gymId
    ) {
        List<AttendanceDto> result = attendanceService.getCurrentlyPresent(gymId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/statistics")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @PathVariable Long gymId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) {
            date = LocalDate.now();
        }
        Map<String, Object> result = attendanceService.getAttendanceStatistics(gymId, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/weekly")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Long>> getWeeklyAttendance(
            @PathVariable Long gymId
    ) {
        Map<String, Long> result = attendanceService.getWeeklyAttendance(gymId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/date-range")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Page<AttendanceDto>> getAttendanceByDateRange(
            @PathVariable Long gymId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkIn"));
        Page<AttendanceDto> result = attendanceService.getAttendanceByDateRange(
                gymId, startDate, endDate, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/date")
    // @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();
        
        // TODO: Implement in AttendanceService
        List<AttendanceDto> attendances = List.of();
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/summary")
    // @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getAttendenceSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }
        long activeMembers = userRepository.findAllActiveUsers(Pageable.unpaged()).getTotalElements();

        Map<String, Object> response = new HashMap<>();
        response.put("activeMembers", activeMembers);
        return ResponseEntity.ok(response);
    }
}
