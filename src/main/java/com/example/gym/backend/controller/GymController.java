package com.example.gym.backend.controller;

import com.example.gym.backend.dto.GymDto;
import com.example.gym.backend.service.GymService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gym/gyms")
@RequiredArgsConstructor
@Slf4j
public class GymController {

    private final GymService gymService;

    @PostMapping("/create")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GymDto> createGym(@Valid @RequestBody GymDto gymDto) {
        log.info("Creating new gym: {}", gymDto.getName());
        GymDto createdGym = gymService.createGym(gymDto);
        return new ResponseEntity<>(createdGym, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GymDto> getGymById(@PathVariable Long id) {
        log.info("Fetching gym with ID: {}", id);
        GymDto gym = gymService.getGymById(id);
        return ResponseEntity.ok(gym);
    }

    @GetMapping("/active")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<GymDto>> getAllActiveGyms() {
        log.info("Fetching all active gyms");
        List<GymDto> gyms = gymService.getAllActiveGyms();
        return ResponseEntity.ok(gyms);
    }

    @GetMapping("/all")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getAllGyms(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Fetching all gyms with pagination: page={}, size={}", page, size);
        
        Page<GymDto> gymPage = gymService.getAllGymsPaginated(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", gymPage.getContent());
        response.put("totalElements", gymPage.getTotalElements());
        response.put("totalPages", gymPage.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GymDto> updateGym(
            @PathVariable Long id,
            @Valid @RequestBody GymDto gymDto) {
        log.info("Updating gym with ID: {}", id);
        GymDto updatedGym = gymService.updateGym(id, gymDto);
        return ResponseEntity.ok(updatedGym);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGym(@PathVariable Long id) {
        log.info("Deleting gym with ID: {}", id);
        gymService.deleteGym(id);
        return ResponseEntity.noContent().build();
    }
}
