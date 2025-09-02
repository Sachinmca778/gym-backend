package com.example.gym.backend.controller;

import com.example.gym.backend.dto.AuthRequestDto;
import com.example.gym.backend.dto.AuthResponseDto;
import com.example.gym.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        log.info("Login attempt for user: {}", request.getUsername());
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestParam String refreshToken) {
        log.info("Token refresh requested");
        AuthResponseDto response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String token) {
        log.info("Logout requested");
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}