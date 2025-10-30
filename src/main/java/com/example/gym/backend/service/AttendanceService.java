package com.example.gym.backend.service;

import com.example.gym.backend.dto.AttendanceDto;
import com.example.gym.backend.entity.Attendance;
import com.example.gym.backend.entity.Member;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.AttendanceRepository;
import com.example.gym.backend.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    public AttendanceDto checkIn(Long memberId, AttendanceDto attendanceDto) {
        log.info("Recording check-in for member ID: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setMethod(attendanceDto.getMethod() != null ? attendanceDto.getMethod() : CheckInMethod.MANUAL);
        attendance.setNotes(attendanceDto.getNotes());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Check-in recorded successfully for member ID: {}", memberId);

        return convertToDto(savedAttendance);
    }

    public AttendanceDto checkOut(Long attendanceId) {
        log.info("Recording check-out for attendance ID: {}", attendanceId);

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + attendanceId));

        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("Member has already checked out");
        }

        LocalDateTime checkOut = LocalDateTime.now();
        attendance.setCheckOut(checkOut);

        // Calculate duration
        long durationMinutes = ChronoUnit.MINUTES.between(attendance.getCheckIn(), checkOut);
        attendance.setDurationMinutes((int) durationMinutes);

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        log.info("Check-out recorded successfully for attendance ID: {}", attendanceId);

        return convertToDto(updatedAttendance);
    }

    public List<AttendanceDto> getMemberAttendance(Long memberId, LocalDate date) {
        log.info("Fetching attendance for member ID: {} on date: {}", memberId, date);
        List<Attendance> attendances = attendanceRepository.findByMemberIdAndDate(memberId, date);
        return attendances.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<AttendanceDto> getAttendanceByDate(LocalDate date) {
        log.info("Fetching all attendance for date: {}", date);
        List<Attendance> attendances = attendanceRepository.findByDate(date);
        return attendances.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public long getDailyAttendanceCount(LocalDate date) {
        return attendanceRepository.countByDate(date);
    }

    public long getMemberAttendanceCount(Long memberId, LocalDate date) {
        return attendanceRepository.countByMemberIdAndDate(memberId, date);
    }

    private AttendanceDto convertToDto(Attendance attendance) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(attendance.getId());
        dto.setMemberId(attendance.getMember().getId());
        dto.setMemberName(attendance.getMember().getFirstName() + " " + attendance.getMember().getLastName());
        dto.setCheckIn(attendance.getCheckIn());
        dto.setCheckOut(attendance.getCheckOut());
        dto.setDurationMinutes(attendance.getDurationMinutes());
        dto.setMethod(attendance.getMethod());
        dto.setNotes(attendance.getNotes());
        dto.setCreatedAt(attendance.getCreatedAt());
        return dto;
    }
}