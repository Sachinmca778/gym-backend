package com.example.gym.backend.controller;

import com.example.gym.backend.dto.AuthRequestDto;
import com.example.gym.backend.dto.AuthResponseDto;
import com.example.gym.backend.dto.RegisterUserDto;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.service.AuthService;
import com.example.gym.backend.service.RedisTokenService;
import com.example.gym.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gym/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final RedisTokenService redisTokenService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserDto dto) {
        log.info("Registration attempt for user: {}", dto.getUsername());
        User savedUser = userService.createUser(dto);
        return ResponseEntity.ok(savedUser);
    }

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

    // Redis Token Management Endpoints for Mobile App
    @PostMapping("/store-token")
    public ResponseEntity<Map<String, String>> storeToken(
            @RequestParam String username,
            @RequestParam String tokenType,
            @RequestParam String token) {
        log.info("Storing {} token for user: {}", tokenType, username);
        
        if ("access".equals(tokenType)) {
            redisTokenService.storeAccessToken(username, token);
        } else if ("refresh".equals(tokenType)) {
            redisTokenService.storeRefreshToken(username, token);
        }
        
        return ResponseEntity.ok(Map.of("status", "stored", "tokenType", tokenType));
    }

    @GetMapping("/get-token/{username}/{tokenType}")
    public ResponseEntity<Map<String, String>> getToken(
            @PathVariable String username,
            @PathVariable String tokenType) {
        log.info("Getting {} token for user: {}", tokenType, username);
        
        String token = "access".equals(tokenType) 
            ? redisTokenService.getAccessToken(username)
            : redisTokenService.getRefreshToken(username);
        
        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token, "tokenType", tokenType));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete-tokens/{username}")
    public ResponseEntity<Map<String, String>> deleteTokens(@PathVariable String username) {
        log.info("Deleting tokens for user: {}", username);
        redisTokenService.deleteTokens(username);
        return ResponseEntity.ok(Map.of("status", "deleted", "username", username));
    }

    @GetMapping("/validate-token/{username}/{tokenType}/{token}")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @PathVariable String username,
            @PathVariable String tokenType,
            @PathVariable String token) {
        log.info("Validating {} token for user: {}", tokenType, username);
        
        boolean isValid = "access".equals(tokenType)
            ? redisTokenService.validateAccessToken(username, token)
            : redisTokenService.validateRefreshToken(username, token);
        
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
