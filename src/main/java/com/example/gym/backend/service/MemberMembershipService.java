package com.example.gym.backend.service;

import com.example.gym.backend.dto.MemberMembershipDto;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.entity.Member;
import com.example.gym.backend.entity.MemberMembership;
import com.example.gym.backend.entity.MembershipPlan;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.exception.ValidationException;
import com.example.gym.backend.repository.GymRepository;
import com.example.gym.backend.repository.MemberMembershipRepository;
import com.example.gym.backend.repository.MemberRepository;
import com.example.gym.backend.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberMembershipService {

    private final MemberMembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final GymRepository gymRepository;

    public MemberMembershipDto createMembership(MemberMembershipDto dto) {
        log.info("Creating new membership for member ID: {}", dto.getMemberId());

        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + dto.getMemberId()));

        MembershipPlan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + dto.getPlanId()));

        // ========== VALIDATION 1: Prevent multiple ACTIVE memberships per member per gym ==========
        if (dto.getGymId() != null && dto.getStatus() != null && "ACTIVE".equals(dto.getStatus())) {
            boolean hasActiveMembership = membershipRepository.existsActiveMembershipByMemberAndGym(
                    member.getId(), dto.getGymId());
            if (hasActiveMembership) {
                throw new ValidationException("Member already has an active membership in this gym. " +
                        "Please cancel or wait for the current membership to expire before creating a new one.");
            }
        }

        // ========== VALIDATION 2: Validate member's gym matches membership's gym ==========
        if (member.getGym() != null && dto.getGymId() != null) {
            if (!member.getGym().getId().equals(dto.getGymId())) {
                throw new ValidationException("Member belongs to gym " + member.getGym().getId() + 
                        " but membership is being created for gym " + dto.getGymId());
            }
        }

        MemberMembership membership = new MemberMembership();
        membership.setMember(member);
        membership.setPlan(plan);
        membership.setStartDate(LocalDate.parse(dto.getStartDate()));
        membership.setEndDate(LocalDate.parse(dto.getEndDate()));
        membership.setAmountPaid(dto.getAmountPaid());
        membership.setAutoRenewal(dto.isAutoRenewal());
        membership.setStatus(MemberMembership.MembershipStatus.ACTIVE);

        // Set gym if gymId is provided
        if (dto.getGymId() != null) {
            Gym gym = gymRepository.findById(dto.getGymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + dto.getGymId()));
            membership.setGym(gym);
        }

        MemberMembership savedMembership = membershipRepository.save(membership);
        log.info("Membership created successfully with ID: {}", savedMembership.getId());

        return convertToDto(savedMembership);
    }

    public List<MemberMembershipDto> getAllMemberships() {
        log.info("Fetching all memberships");
        List<MemberMembership> memberships = membershipRepository.findAll();
        return memberships.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public MemberMembershipDto getMembershipById(Long id) {
        log.info("Fetching membership with ID: {}", id);
        MemberMembership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with ID: " + id));
        return convertToDto(membership);
    }

    public List<MemberMembershipDto> getMembershipsByMemberId(Long memberId) {
        log.info("Fetching memberships for member ID: {}", memberId);
        List<MemberMembership> memberships = membershipRepository.findByMemberId(memberId);
        return memberships.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public MemberMembershipDto updateMembership(Long id, MemberMembershipDto dto) {
        log.info("Updating membership with ID: {}", id);
        MemberMembership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with ID: " + id));

        // ========== VALIDATION: Prevent multiple ACTIVE memberships (for updates) ==========
        if (dto.getStatus() != null && "ACTIVE".equals(dto.getStatus())) {
            // If changing status to ACTIVE, check for existing active memberships
            Long gymId = membership.getGym() != null ? membership.getGym().getId() : dto.getGymId();
            if (gymId != null) {
                List<MemberMembership> activeMemberships = membershipRepository
                        .findActiveMembershipsByMemberAndGym(membership.getMember().getId(), gymId);
                // If there's more than 1 active membership (the one we're updating), throw error
                if (activeMemberships.size() > 1 || 
                    (activeMemberships.size() == 1 && !activeMemberships.get(0).getId().equals(id))) {
                    throw new ValidationException("Cannot activate this membership. Member already has an active membership in this gym.");
                }
            }
        }

        if (dto.getStartDate() != null) {
            membership.setStartDate(LocalDate.parse(dto.getStartDate()));
        }
        if (dto.getEndDate() != null) {
            membership.setEndDate(LocalDate.parse(dto.getEndDate()));
        }
        if (dto.getAmountPaid() != null) {
            membership.setAmountPaid(dto.getAmountPaid());
        }
        membership.setAutoRenewal(dto.isAutoRenewal());
        if (dto.getStatus() != null) {
            membership.setStatus(MemberMembership.MembershipStatus.valueOf(dto.getStatus()));
        }

        MemberMembership updatedMembership = membershipRepository.save(membership);
        log.info("Membership updated successfully with ID: {}", updatedMembership.getId());

        return convertToDto(updatedMembership);
    }

    private MemberMembershipDto convertToDto(MemberMembership membership) {
        MemberMembershipDto dto = new MemberMembershipDto();
        dto.setId(membership.getId());
        dto.setMemberId(membership.getMember().getId());
        dto.setPlanId(membership.getPlan().getId());
        dto.setStartDate(membership.getStartDate().toString());
        dto.setEndDate(membership.getEndDate().toString());
        dto.setAmountPaid(membership.getAmountPaid());
        dto.setStatus(membership.getStatus().toString());
        dto.setAutoRenewal(membership.isAutoRenewal());
        dto.setCreatedAt(membership.getCreatedAt());
        if (membership.getGym() != null) {
            dto.setGymId(membership.getGym().getId());
        }
        return dto;
    }
}

