package com.example.gym.backend.service;


import com.example.gym.backend.dto.MembershipPlanDto;
import com.example.gym.backend.entity.MembershipPlan;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MembershipPlanService {

    private final MembershipPlanRepository planRepository;

    public MembershipPlanDto createPlan(MembershipPlanDto planDto) {
        log.info("Creating new membership plan: {}", planDto.getName());

        MembershipPlan plan = new MembershipPlan();
        plan.setName(planDto.getName());
        plan.setDescription(planDto.getDescription());
        plan.setDurationMonths(planDto.getDurationMonths());
        plan.setPrice(planDto.getPrice());

        // join features list into comma separated string
        if (planDto.getFeatures() != null) {
            plan.setFeatures(String.join(",", planDto.getFeatures()));
        } else {
            plan.setFeatures(null);
        }

        plan.setActive(planDto.isActive());


        MembershipPlan savedPlan = planRepository.save(plan);
        log.info("Membership plan created successfully with ID: {}", savedPlan.getId());

        return convertToDto(savedPlan);
    }

    public MembershipPlanDto getPlanById(Long id) {
        log.info("Fetching membership plan with ID: {}", id);
        MembershipPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + id));
        return convertToDto(plan);
    }

    public List<MembershipPlanDto> getAllActivePlans() {
        log.info("Fetching all active membership plans");
//        List<MembershipPlan> plans = planRepository.findByIsActiveTrue();
        List<MembershipPlan> plans = planRepository.findActivePlans();

        return plans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<MembershipPlanDto> getAllPlans() {
        log.info("Fetching all membership plans");
        List<MembershipPlan> plans = planRepository.findAll();
        return plans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<MembershipPlanDto> getPlansByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching plans with price range: {} - {}", minPrice, maxPrice);
        List<MembershipPlan> plans = planRepository.findActivePlansByPriceRange(minPrice.doubleValue(), maxPrice.doubleValue());
        return plans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public MembershipPlanDto updatePlan(Long id, MembershipPlanDto planDto) {
        log.info("Updating membership plan with ID: {}", id);
        MembershipPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + id));

        plan.setName(planDto.getName());
        plan.setDescription(planDto.getDescription());
        plan.setDurationMonths(planDto.getDurationMonths());
        plan.setPrice(planDto.getPrice());
        plan.setFeatures(planDto.getFeatures());
        // join features list into comma separated string
        // if (planDto.getFeatures() != null) {
        //     plan.setFeatures(String.join(",", planDto.getFeatures()));
        // } else {
        //     plan.setFeatures(null);
        // }
        plan.setActive(planDto.isActive());

        MembershipPlan updatedPlan = planRepository.save(plan);
        log.info("Membership plan updated successfully with ID: {}", updatedPlan.getId());

        return convertToDto(updatedPlan);
    }

    public void deletePlan(Long id) {
        log.info("Deleting membership plan with ID: {}", id);
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Membership plan not found with ID: " + id);
        }
        planRepository.deleteById(id);
        log.info("Membership plan deleted successfully with ID: {}", id);
    }

    private MembershipPlanDto convertToDto(MembershipPlan plan) {
        MembershipPlanDto dto = new MembershipPlanDto();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setDurationMonths(plan.getDurationMonths());
        dto.setPrice(plan.getPrice());
        dto.setFeatures(plan.getFeatures());
        // join features list into comma separated string
        // if (plan.getFeatures() != null) {
        //     plan.setFeatures(String.join(",", plan.getFeatures()));
        // } else {
        //     plan.setFeatures(null);
        // }
        dto.setActive(plan.isActive());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }
}