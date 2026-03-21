package com.example.gym.backend.controller;

import com.example.gym.backend.dto.AttendanceDto;
import com.example.gym.backend.dto.MemberDto;
import com.example.gym.backend.service.AttendanceService;
import com.example.gym.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.gym.backend.repository.UserRepository;
        import java.time.LocalDate;
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

    @PostMapping("/check-in/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public AttendanceDto checkIn(
            @PathVariable Long gymId,
            @PathVariable Long userId,
            @RequestBody AttendanceDto dto
    ) {
        return attendanceService.checkIn(gymId, userId, dto);
    }

    @PostMapping("/check-out/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public AttendanceDto checkOut(
            @PathVariable Long gymId,
            @PathVariable Long userId
    ) {
        return attendanceService.checkOut(gymId, userId);
    }

    @GetMapping("/current/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public AttendanceDto getCurrentAttendance(
            @PathVariable Long gymId,
            @PathVariable Long userId
    ) {
        return attendanceService.getCurrentAttendance(gymId, userId);
    }

    @GetMapping("/today/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public AttendanceDto getTodayCompletedAttendance(
            @PathVariable Long gymId,
            @PathVariable Long userId
    ) {
        return attendanceService.getTodayCompletedAttendance(gymId, userId);
    }

    @GetMapping("/date")
    // @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching all attendance for date: {}", date);

        if (date == null) {
            date = LocalDate.now();
        }
        // TODO: Implement this method in AttendanceService
        // List<AttendanceDto> attendances = attendanceService.getAttendanceByDate(date);
        List<AttendanceDto> attendances = List.of(); // Return empty list for now
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/summary")
    // @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getAttendenceSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        // TODO: Implement these methods in AttendanceService
        // long dailyAttendanceCount = attendanceService.getDailyAttendanceCount(date);
        // Double dailyAverageDuration = attendanceService.getFindAvgDurationByDate(date);
        
        long activeMembers = userRepository.findAllActiveUsers(Pageable.unpaged()).getTotalElements();
        // long dailyAbsentMember = activeMembers - dailyAttendanceCount;

        Map<String, Object> response = new HashMap<>();
        // response.put("dailyAttendanceCount", dailyAttendanceCount);
        response.put("activeMembers", activeMembers);
        // response.put("dailyAverageDuration", dailyAverageDuration);
        // response.put("dailyAbsentMember", dailyAbsentMember);
        return ResponseEntity.ok(response);
    }
}
