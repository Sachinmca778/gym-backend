package com.example.gym.backend.controller;


import com.example.gym.backend.dto.MembershipPlanDto;
import com.example.gym.backend.service.MembershipPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gym/membership_plans")
@RequiredArgsConstructor
@Slf4j
public class MembershipPlanController {

    private final MembershipPlanService planService;

    @PostMapping("/create")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MembershipPlanDto> createPlan(@Valid @RequestBody MembershipPlanDto planDto) {
        log.info("Creating new membership plan: {}", planDto.getName());
        MembershipPlanDto createdPlan = planService.createPlan(planDto);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<MembershipPlanDto> getPlanById(@PathVariable Long id) {
        log.info("Fetching membership plan with ID: {}", id);
        MembershipPlanDto plan = planService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/active")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<MembershipPlanDto>> getAllActivePlans() {
        log.info("Fetching all active membership plans");
        List<MembershipPlanDto> plans = planService.getAllActivePlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/all")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getAllPlans(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Fetching all membership plans with pagination: page={}, size={}", page, size);
        
        Page<MembershipPlanDto> planPage = planService.getAllPlansPaginated(page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", planPage.getContent());
        response.put("totalElements", planPage.getTotalElements());
        response.put("totalPages", planPage.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/price-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TRAINER', 'RECEPTIONIST')")
    public ResponseEntity<List<MembershipPlanDto>> getPlansByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        log.info("Searching plans with price range: {} - {}", minPrice, maxPrice);
        List<MembershipPlanDto> plans = planService.getPlansByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(plans);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MembershipPlanDto> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody MembershipPlanDto planDto) {
        log.info("Updating membership plan with ID: {}", id);
        MembershipPlanDto updatedPlan = planService.updatePlan(id, planDto);
        return ResponseEntity.ok(updatedPlan);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        log.info("Deleting membership plan with ID: {}", id);
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}