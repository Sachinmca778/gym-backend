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


        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

//        MemberDto member = memberService.getMemberByUserId(user.getId());
//        Long memberId = member.getId();

        // Get gymId (null for ADMIN, not null for other roles)
        Long gymId = null;
        if (user.getGym() != null) {
            gymId = user.getGym().getId();
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("name", fullName);
//        claims.put("memberId",memberId);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userId(user.getId())
                .username(user.getUsername())
                .name(fullName)
//                .memberId(memberId)
                .role(user.getRole().name())
                .gymId(gymId) // NULL for ADMIN, NOT NULL for other roles
                .build();
    }

    public AuthResponseDto refreshToken(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newAccessToken = jwtUtil.generateToken(userDetails);

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
        // In a real application, you might want to blacklist the token
        log.info("User logged out successfully");
    }
}