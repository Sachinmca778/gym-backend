package com.example.gym.backend.controller;

import com.example.gym.backend.dto.UserSearchDto;
import com.example.gym.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gym/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDto>> searchUsers(@RequestParam String searchTerm) {
        log.info("Searching users with term: {}", searchTerm);
        List<UserSearchDto> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }
}

