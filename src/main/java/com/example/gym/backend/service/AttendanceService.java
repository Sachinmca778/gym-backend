package com.example.gym.backend.service;

import com.example.gym.backend.dto.AttendanceDto;
import com.example.gym.backend.entity.Attendance;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.AttendanceRepository;
import com.example.gym.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gym.backend.entity.Attendance.CheckInMethod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        log.info("Checking in user {} at gym {}", userId, gymId);

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
        log.info("Checking out user {} from gym {}", userId, gymId);

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
    public List<AttendanceDto> getTodayCompletedAttendance(Long gymId, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<Attendance> attendances = attendanceRepository
                .findCompletedAttendanceByUserAndDate(userId, startOfDay, endOfDay);

        if (attendances.isEmpty()) {
            return List.of();
        }

        return attendances.stream()
                .filter(a -> a.getGym().getId().equals(gymId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ================= GET TODAY'S ATTENDANCE LIST (ALL USERS) =================
    public Page<AttendanceDto> getTodayAttendance(Long gymId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Page<Attendance> attendancePage = attendanceRepository.findAllByGymAndDate(
                gymId, startOfDay, endOfDay, pageable);

        return attendancePage.map(this::toDto);
    }

    // ================= GET CURRENTLY PRESENT MEMBERS =================
    public List<AttendanceDto> getCurrentlyPresent(Long gymId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<Attendance> presentMembers = attendanceRepository.findCurrentlyPresent(
                gymId, startOfDay, endOfDay);

        return presentMembers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ================= GET ATTENDANCE STATISTICS =================
    public Map<String, Object> getAttendanceStatistics(Long gymId, LocalDate date) {
        log.info("Fetching attendance statistics for gym {} on date {}", gymId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        long totalCheckIns = attendanceRepository.countTodayAttendance(gymId, startOfDay, endOfDay);
        long currentlyPresent = attendanceRepository.countCurrentlyPresent(gymId, startOfDay, endOfDay);
        Double avgDuration = attendanceRepository.getAverageDuration(gymId, startOfDay, endOfDay);

        // Get peak hours
        List<Object[]> peakHoursData = attendanceRepository.getPeakHours(gymId, startOfDay, endOfDay);
        Map<Integer, Long> peakHours = new HashMap<>();
        for (Object[] row : peakHoursData) {
            peakHours.put((Integer) row[0], (Long) row[1]);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCheckIns", totalCheckIns);
        stats.put("currentlyPresent", currentlyPresent);
        stats.put("averageDuration", avgDuration != null ? avgDuration.intValue() : 0);
        stats.put("peakHours", peakHours);

        return stats;
    }

    // ================= GET WEEKLY ATTENDANCE =================
    public Map<String, Long> getWeeklyAttendance(Long gymId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        Map<String, Long> weeklyData = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            long count = attendanceRepository.countTodayAttendance(gymId, startOfDay, endOfDay);
            String dayName = date.getDayOfWeek().toString().substring(0, 3); // e.g., "MON"
            weeklyData.put(dayName, count);
        }

        return weeklyData;
    }

    // ================= GET ATTENDANCE BY DATE RANGE =================
    public Page<AttendanceDto> getAttendanceByDateRange(Long gymId, LocalDate startDate, 
                                                        LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        Page<Attendance> attendancePage = attendanceRepository.findByDateRange(
                gymId, startDateTime, endDateTime, pageable);

        return attendancePage.map(this::toDto);
    }

    private AttendanceDto toDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setUserId(a.getUser().getId());
        dto.setUserName(a.getUser().getFirstName() + " " + a.getUser().getLastName());
        dto.setUserEmail(a.getUser().getEmail());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setDurationMinutes(a.getDurationMinutes());
        dto.setMethod(a.getMethod().name());
        return dto;
    }
}
