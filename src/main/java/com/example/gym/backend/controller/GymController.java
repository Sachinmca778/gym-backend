package com.example.gym.backend.controller;

import com.example.gym.backend.dto.GymDto;
import com.example.gym.backend.service.GymService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gym/gyms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
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
    public ResponseEntity<List<GymDto>> getAllGyms() {
        log.info("Fetching all gyms");
        List<GymDto> gyms = gymService.getAllGyms();
        return ResponseEntity.ok(gyms);
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
