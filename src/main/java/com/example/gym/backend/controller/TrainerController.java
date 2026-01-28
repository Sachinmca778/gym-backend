package com.example.gym.backend.controller;


import com.example.gym.backend.dto.TrainerDto;
import com.example.gym.backend.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/gym/trainers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TrainerDto> createTrainer(@Valid @RequestBody TrainerDto trainerDto) {
        log.info("Creating new trainer: {} {}", trainerDto.getFirstName(), trainerDto.getLastName());
        TrainerDto createdTrainer = trainerService.createTrainer(trainerDto);
        return new ResponseEntity<>(createdTrainer, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<TrainerDto> getTrainerById(@PathVariable Long id) {
        log.info("Fetching trainer with ID: {}", id);
        TrainerDto trainer = trainerService.getTrainerById(id);
        return ResponseEntity.ok(trainer);
    }

    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<TrainerDto>> getAllTrainers() {
        log.info("Fetching all trainers");
        List<TrainerDto> trainers = trainerService.getAllTrainers();
        return ResponseEntity.ok(trainers);
    }

    @GetMapping("/active")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<TrainerDto>> getAllActiveTrainers() {
        log.info("Fetching all active trainers");
        List<TrainerDto> trainers = trainerService.getAllActiveTrainers();
        return ResponseEntity.ok(trainers);
    }

    @GetMapping("/specialization/{specialization}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<TrainerDto>> getTrainersBySpecialization(@PathVariable String specialization) {
        log.info("Fetching trainers by specialization: {}", specialization);
        List<TrainerDto> trainers = trainerService.getTrainersBySpecialization(specialization);
        return ResponseEntity.ok(trainers);
    }

    @GetMapping("/top-rated")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<TrainerDto>> getTopRatedTrainers(@RequestParam(defaultValue = "4.0") Double minRating) {
        log.info("Fetching top rated trainers with minimum rating: {}", minRating);
        List<TrainerDto> trainers = trainerService.getTopRatedTrainers(BigDecimal.valueOf(minRating));
        return ResponseEntity.ok(trainers);
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TrainerDto> updateTrainer(@PathVariable Long id, @Valid @RequestBody TrainerDto trainerDto) {
        log.info("Updating trainer with ID: {}", id);
        TrainerDto updatedTrainer = trainerService.updateTrainer(id, trainerDto);
        return ResponseEntity.ok(updatedTrainer);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteTrainer(@PathVariable Long id) {
        log.info("Deleting trainer with ID: {}", id);
        trainerService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }
}