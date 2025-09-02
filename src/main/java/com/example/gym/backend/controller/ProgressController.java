package com.example.gym.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProgressController {

    // TODO: Implement progress tracking functionality
    // This will include workout sessions, measurements, goals tracking

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Progress tracking service is running");
    }
}