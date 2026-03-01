package com.example.gym.backend.service;

import com.example.gym.backend.dto.RegisterUserDto;
import com.example.gym.backend.dto.UserSearchDto;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.repository.GymRepository;
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
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(RegisterUserDto dto) {
        log.info("Creating user: {}", dto.getUsername());
        
        // Check if user already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new User entity from DTO
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        
        // Set default role if not provided
        if (dto.getRole() == null) {
            user.setRole(User.UserRole.ADMIN);
        } else {
            user.setRole(dto.getRole());
        }
        
        // Set active status
        user.setActive(true);
        
        // Set gym if gymId is provided
        if (dto.getGym() != null) {
            Gym gym = gymRepository.findById(dto.getGym())
                    .orElseThrow(() -> new RuntimeException("Gym not found with id: " + dto.getGym()));
            user.setGym(gym);
        }
        
        return userRepository.save(user);
    }

    public List<UserSearchDto> searchUsers(String searchTerm) {
        log.info("Searching users with term: {}", searchTerm);
        List<User> users = userRepository.searchUsers(searchTerm);
        return users.stream()
                .map(this::convertToSearchDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active users - for admin to select when creating a member
     * ADMIN/MANAGER/RECEPTIONIST can see all users in their gym
     * SUPER_USER can see all users
     */
    public List<UserSearchDto> getAllUsers(User currentUser) {
        log.info("Getting all users for user: {}", currentUser.getUsername());
        
        List<User> users;
        
        // If SUPER_USER, get all users
        if (currentUser.getRole() == User.UserRole.SUPER_USER) {
            users = userRepository.findAllActive();
        } else if (currentUser.getGym() != null) {
            // Get users for the specific gym
            users = userRepository.findActiveByGymId(currentUser.getGym().getId());
        } else {
            users = List.of();
        }
        
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
        dto.setGymId(user.getGym() != null ? user.getGym().getId() : null);
        return dto;
    }
}
