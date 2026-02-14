package com.example.gym.backend.service;

import com.example.gym.backend.dto.UserSearchDto;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        
        // Check if user already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Encode password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // Set default role if not provided
        if (user.getRole() == null) {
            user.setRole(User.UserRole.ADMIN);
        }
        
        // Set active status
        user.setActive(true);
        
        return userRepository.save(user);
    }

    public List<UserSearchDto> searchUsers(String searchTerm) {
        log.info("Searching users with term: {}", searchTerm);
        List<User> users = userRepository.searchUsers(searchTerm);
        return users.stream()
                .map(this::convertToSearchDto)
                .collect(Collectors.toList());
    }

    private UserSearchDto convertToSearchDto(User user) {
        UserSearchDto dto = new UserSearchDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        return dto;
    }
}
