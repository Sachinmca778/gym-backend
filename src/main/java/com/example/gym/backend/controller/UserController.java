package com.example.gym.backend.controller;

import com.example.gym.backend.dto.UserSearchDto;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.repository.UserRepository;
import com.example.gym.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gym/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Get current authenticated user from security context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        // Fallback: try to get user by username from authentication
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDto>> searchUsers(@RequestParam String searchTerm) {
        log.info("Searching users with term: {}", searchTerm);
        List<UserSearchDto> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }

    /**
     * Get all users - for admin to select when creating a member
     * ADMIN, MANAGER, RECEPTIONIST can access
     * SUPER_USER can also access
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('SUPER_USER', 'ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<UserSearchDto>> getAllUsers() {
        log.info("Fetching all users");
        User currentUser = getCurrentUser();
        List<UserSearchDto> users = userService.getAllUsers(currentUser);
        return ResponseEntity.ok(users);
    }
}

