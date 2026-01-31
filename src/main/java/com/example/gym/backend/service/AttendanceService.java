package com.example.gym.backend.service;

import com.example.gym.backend.dto.AttendanceDto;
import com.example.gym.backend.entity.Attendance;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.AttendanceRepository;
import com.example.gym.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gym.backend.entity.Attendance.CheckInMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    // ================= CHECK IN =================
    @Transactional
    public AttendanceDto checkIn(Long gymId, Long userId, AttendanceDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getGym().getId().equals(gymId)) {
            throw new IllegalStateException("User does not belong to this gym");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        attendanceRepository
                .findOpenAttendance(userId, startOfDay, endOfDay)
                .ifPresent(a -> {
                    throw new IllegalStateException("User already checked in today");
                });

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setGym(user.getGym());
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setMethod(
                Attendance.CheckInMethod.valueOf(dto.getMethod())
        );

        return toDto(attendanceRepository.save(attendance));
    }

    // ================= CHECK OUT =================
    @Transactional
    public AttendanceDto checkOut(Long gymId, Long userId) {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Attendance attendance = attendanceRepository
                .findOpenAttendance(userId, startOfDay, endOfDay)
                .orElseThrow(() -> new IllegalStateException("No active check-in found"));

        if (!attendance.getGym().getId().equals(gymId)) {
            throw new IllegalStateException("Gym mismatch");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOut(now);
        attendance.setDurationMinutes(
                (int) ChronoUnit.MINUTES.between(attendance.getCheckIn(), now)
        );

        return toDto(attendanceRepository.save(attendance));
    }

    // ================= GET CURRENT OPEN ATTENDANCE =================
    public AttendanceDto getCurrentAttendance(Long gymId, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Attendance attendance = attendanceRepository
                .findOpenAttendance(userId, startOfDay, endOfDay)
                .orElse(null);

        if (attendance == null) {
            return null;
        }

        if (!attendance.getGym().getId().equals(gymId)) {
            throw new IllegalStateException("Gym mismatch");
        }

        return toDto(attendance);
    }

    

    // ================= GET TODAY'S COMPLETED ATTENDANCE =================
    public AttendanceDto getTodayCompletedAttendance(Long gymId, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<Attendance> attendances = attendanceRepository
                .findCompletedAttendanceByUserAndDate(userId, startOfDay, endOfDay);

        if (attendances.isEmpty()) {
            return null;
        }

        // Return the most recent completed attendance
        Attendance attendance = attendances.get(0);

        if (!attendance.getGym().getId().equals(gymId)) {
            throw new IllegalStateException("Gym mismatch");
        }

        return toDto(attendance);
    }

    private AttendanceDto toDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setUserId(a.getUser().getId());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setDurationMinutes(a.getDurationMinutes());
        dto.setMethod(a.getMethod().name());
        return dto;
    }

    //  public List<AttendanceDto> getMemberAttendance(Long memberId, LocalDate date) {
    //     log.info("Fetching attendance for member ID: {} on date: {}", memberId, date);
    //     List<Attendance> attendances = attendanceRepository.findByMemberIdAndDate(memberId, date);
    //     return attendances.stream().map(this::convertToDto).collect(Collectors.toList());
    // }

    // public List<AttendanceDto> getAttendanceByDate(LocalDate date) {
    //     log.info("Fetching all attendance for date: {}", date);
    //     List<Attendance> attendances = attendanceRepository.findByDate(date);
    //     return attendances.stream().map(this::convertToDto).collect(Collectors.toList());
    // }

    // public long getDailyAttendanceCount(LocalDate date) {
    //     return attendanceRepository.countByDate(date);
    // }

    // public long getMemberAttendanceCount(Long memberId, LocalDate date) {
    //     return attendanceRepository.countByMemberIdAndDate(memberId, date);
    // }

    // public Double getFindAvgDurationByDate(LocalDate date) {
    //     return attendanceRepository.findAvgDurationByDate(date);
    // }
}
