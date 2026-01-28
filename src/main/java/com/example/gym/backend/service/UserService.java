package com.example.gym.backend.service;

import com.example.gym.backend.dto.UserDto;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.exception.ValidationException;
import com.example.gym.backend.repository.GymRepository;
import com.example.gym.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing users with role-based validation for gym_id
 * 
 * Business Rules:
 * - ADMIN: gym_id = NULL (super admin, can access all gyms)
 * - MANAGER, RECEPTIONIST, TRAINER, MEMBER: gym_id = NOT NULL (specific to one gym)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validate gym_id based on user role
     * 
     * @param role The user's role
     * @param gymId The gym_id to validate (can be null)
     * @throws ValidationException if validation fails
     */
    public void validateGymIdForRole(User.UserRole role, Long gymId) {
        if (role == null) {
            throw new ValidationException("User role is required");
        }

        switch (role) {
            case ADMIN:
                // ADMIN should NOT have a gym_id
                if (gymId != null) {
                    throw new ValidationException("ADMIN role should not have a gym_id. " +
                            "Admins are super users with access to all gyms.");
                }
                break;
                
            case MANAGER:
            case RECEPTIONIST:
            case TRAINER:
            case MEMBER:
                // These roles MUST have a gym_id
                if (gymId == null) {
                    throw new ValidationException(role + " role requires a gym_id. " +
                            "Users with this role must be associated with a specific gym.");
                }
                break;
                
            default:
                throw new ValidationException("Unknown user role: " + role);
        }
    }

    public UserDto createUser(UserDto userDto) {
        log.info("Creating new user: {} with role: {}", userDto.getUsername(), userDto.getRole());

        // ========== VALIDATION 1: Role-based gym_id validation ==========
        User.UserRole role = User.UserRole.valueOf(userDto.getRole());
        validateGymIdForRole(role, userDto.getGymId());

        // ========== VALIDATION 2: Username uniqueness ==========
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new ValidationException("Username '" + userDto.getUsername() + "' already exists");
        }

        // ========== VALIDATION 3: Email uniqueness ==========
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new ValidationException("Email '" + userDto.getEmail() + "' already exists");
            }
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setRole(role);
        user.setActive(true);

        // Set gym if gymId is provided (required for non-ADMIN roles)
        if (userDto.getGymId() != null) {
            Gym gym = gymRepository.findById(userDto.getGymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + userDto.getGymId()));
            user.setGym(gym);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return convertToDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToDto(user);
    }

    public UserDto getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<UserDto> getActiveUsersByRole(String role) {
        log.info("Fetching active users by role: {}", role);
        User.UserRole userRole = User.UserRole.valueOf(role);
        List<User> users = userRepository.findActiveUsersByRole(userRole);
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // ========== VALIDATION: Role-based gym_id validation (for update) ==========
        if (userDto.getRole() != null) {
            User.UserRole newRole = User.UserRole.valueOf(userDto.getRole());
            Long newGymId = userDto.getGymId() != null ? userDto.getGymId() : 
                           (user.getGym() != null ? user.getGym().getId() : null);
            validateGymIdForRole(newRole, newGymId);
        }

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        
        if (userDto.getRole() != null) {
            user.setRole(User.UserRole.valueOf(userDto.getRole()));
        }
        
        user.setActive(userDto.isActive());

        // Update gym if gymId is provided
        if (userDto.getGymId() != null) {
            Gym gym = gymRepository.findById(userDto.getGymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + userDto.getGymId()));
            user.setGym(gym);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return convertToDto(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        if (user.getGym() != null) {
            dto.setGymId(user.getGym().getId());
        }
        return dto;
    }
}

