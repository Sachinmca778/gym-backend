package com.example.gym.backend.service;

import com.example.gym.backend.dto.MemberMembershipDto;
import com.example.gym.backend.entity.Member;
import com.example.gym.backend.entity.MemberMembership;
import com.example.gym.backend.entity.MembershipPlan;
import com.example.gym.backend.exception.ResourceNotFoundException;
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

    public MemberMembershipDto createMembership(MemberMembershipDto dto) {
        log.info("Creating new membership for member ID: {}", dto.getMemberId());

        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + dto.getMemberId()));

        MembershipPlan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with ID: " + dto.getPlanId()));

        MemberMembership membership = new MemberMembership();
        membership.setMember(member);
        membership.setPlan(plan);
        membership.setStartDate(LocalDate.parse(dto.getStartDate()));
        membership.setEndDate(LocalDate.parse(dto.getEndDate()));
        membership.setAmountPaid(dto.getAmountPaid());
        membership.setAutoRenewal(dto.isAutoRenewal());

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
        return dto;
    }
}
