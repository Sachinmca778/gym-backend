package com.example.gym.backend.service;

import com.example.gym.backend.dto.AuthRequestDto;
import com.example.gym.backend.dto.AuthResponseDto;
import com.example.gym.backend.dto.MemberDto;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.repository.UserRepository;
import com.example.gym.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final MemberService memberService;
    private final RedisTokenService redisTokenService;

    public AuthResponseDto login(AuthRequestDto request) {
        log.info("Authenticating user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                (user.getLastName() != null ? user.getLastName() : "");
        fullName = fullName.trim();

        String roleName = user.getRole() != null ? user.getRole().name() : "MEMBER";

        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Store tokens in Redis for validation
        redisTokenService.storeAccessToken(userDetails.getUsername(), accessToken);
        redisTokenService.storeRefreshToken(userDetails.getUsername(), refreshToken);

//        MemberDto member = memberService.getMemberByUserId(user.getId());
//        Long memberId = member.getId();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", roleName);
        claims.put("name", fullName);
        claims.put("gymId",user.getGym() != null ? user.getGym().getId() : null);

        redisTokenService.storeUserSession(userDetails.getUsername(), claims);


        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userId(user.getId())
                .username(user.getUsername())
                .name(fullName)
//                .memberId(memberId)
                .role(roleName)
                .gymId(user.getGym() != null ? user.getGym().getId() : null)
                .build();
    }

    public AuthResponseDto refreshToken(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            
            // Validate refresh token exists in Redis
            if (!redisTokenService.validateRefreshToken(username, refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }
            
            UserDetails userDetails = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newAccessToken = jwtUtil.generateToken(userDetails);
            
            // Update access token in Redis
            redisTokenService.storeAccessToken(username, newAccessToken);

            return AuthResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();
        }
        throw new RuntimeException("Invalid refresh token");
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            
            // Delete tokens from Redis (server-side logout)
            redisTokenService.deleteTokens(username);
            redisTokenService.deleteUserSession(username);
            redisTokenService.deleteUserSession(username);

            log.info("User logged out successfully and tokens removed from Redis: {}", username);
        } else {
            log.info("User logged out successfully");
        }
    }
}