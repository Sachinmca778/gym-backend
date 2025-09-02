package com.example.gym.backend.controller;



import com.example.gym.backend.dto.AttendanceDto;
import com.example.gym.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<AttendanceDto> checkIn(
            @PathVariable Long memberId,
            @RequestBody AttendanceDto attendanceDto) {
        log.info("Recording check-in for member ID: {}", memberId);
        AttendanceDto attendance = attendanceService.checkIn(memberId, attendanceDto);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/check-out/{attendanceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<AttendanceDto> checkOut(@PathVariable Long attendanceId) {
        log.info("Recording check-out for attendance ID: {}", attendanceId);
        AttendanceDto attendance = attendanceService.checkOut(attendanceId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<AttendanceDto>> getMemberAttendance(
            @PathVariable Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching attendance for member ID: {} on date: {}", memberId, date);
        List<AttendanceDto> attendances = attendanceService.getMemberAttendance(memberId, date);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching all attendance for date: {}", date);
        List<AttendanceDto> attendances = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/count/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> getDailyAttendanceCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching daily attendance count for date: {}", date);
        long count = attendanceService.getDailyAttendanceCount(date);
        return ResponseEntity.ok(count);
    }
}
