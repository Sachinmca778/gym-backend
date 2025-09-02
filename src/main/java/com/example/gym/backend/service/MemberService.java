package com.example.gym.backend.service;



import com.example.gym.backend.dto.MemberDto;
import com.example.gym.backend.dto.MemberSearchDto;
import com.example.gym.backend.entity.Member;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.MemberRepository;
import com.example.gym.backend.util.MemberCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberCodeGenerator memberCodeGenerator;

    public MemberDto createMember(MemberDto memberDto) {
        log.info("Creating new member: {}", memberDto.getFirstName() + " " + memberDto.getLastName());

        // Generate unique member code
        String memberCode = memberCodeGenerator.generateUniqueCode();

        Member member = new Member();
        member.setMemberCode(memberCode);
        member.setFirstName(memberDto.getFirstName());
        member.setLastName(memberDto.getLastName());
        member.setEmail(memberDto.getEmail());
        member.setPhone(memberDto.getPhone());
        member.setDateOfBirth(memberDto.getDateOfBirth());
        member.setGender(memberDto.getGender());
        member.setAddress(memberDto.getAddress());
        member.setCity(memberDto.getCity());
        member.setState(memberDto.getState());
        member.setPincode(memberDto.getPincode());
        member.setEmergencyContactName(memberDto.getEmergencyContactName());
        member.setEmergencyContactPhone(memberDto.getEmergencyContactPhone());
        member.setEmergencyContactRelation(memberDto.getEmergencyContactRelation());
        member.setMedicalConditions(memberDto.getMedicalConditions());
        member.setAllergies(memberDto.getAllergies());
        member.setFitnessGoals(memberDto.getFitnessGoals());
        member.setJoinDate(LocalDate.now());
        member.setStatus(Member.MemberStatus.ACTIVE);

        Member savedMember = memberRepository.save(member);
        log.info("Member created successfully with ID: {}", savedMember.getId());

        return convertToDto(savedMember);
    }

    public MemberDto getMemberById(Long id) {
        log.info("Fetching member with ID: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
        return convertToDto(member);
    }

    public MemberDto getMemberByCode(String memberCode) {
        log.info("Fetching member with code: {}", memberCode);
        Member member = memberRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with code: " + memberCode));
        return convertToDto(member);
    }

    public Page<MemberDto> searchMembers(MemberSearchDto searchDto, Pageable pageable) {
        log.info("Searching members with criteria: {}", searchDto);
        Page<Member> members = memberRepository.searchMembers(searchDto.getSearchTerm(), pageable);
        return members.map(this::convertToDto);
    }

    public MemberDto updateMember(Long id, MemberDto memberDto) {
        log.info("Updating member with ID: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));

        member.setFirstName(memberDto.getFirstName());
        member.setLastName(memberDto.getLastName());
        member.setEmail(memberDto.getEmail());
        member.setPhone(memberDto.getPhone());
        member.setDateOfBirth(memberDto.getDateOfBirth());
        member.setGender(memberDto.getGender());
        member.setAddress(memberDto.getAddress());
        member.setCity(memberDto.getCity());
        member.setState(memberDto.getState());
        member.setPincode(memberDto.getPincode());
        member.setEmergencyContactName(memberDto.getEmergencyContactName());
        member.setEmergencyContactPhone(memberDto.getEmergencyContactPhone());
        member.setEmergencyContactRelation(memberDto.getEmergencyContactRelation());
        member.setMedicalConditions(memberDto.getMedicalConditions());
        member.setAllergies(memberDto.getAllergies());
        member.setFitnessGoals(memberDto.getFitnessGoals());

        Member updatedMember = memberRepository.save(member);
        log.info("Member updated successfully with ID: {}", updatedMember.getId());

        return convertToDto(updatedMember);
    }

    public void deleteMember(Long id) {
        log.info("Deleting member with ID: {}", id);
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member not found with ID: " + id);
        }
        memberRepository.deleteById(id);
        log.info("Member deleted successfully with ID: {}", id);
    }

    public List<MemberDto> getMembersWithExpiringMemberships(int daysBeforeExpiry) {
        LocalDate expiryDate = LocalDate.now().plusDays(daysBeforeExpiry);
        List<Member> members = memberRepository.findMembersWithExpiringMemberships(expiryDate);
        return members.stream().map(this::convertToDto).toList();
    }

    public long getActiveMembersCount() {
        return memberRepository.countByStatus(Member.MemberStatus.ACTIVE);
    }

    private MemberDto convertToDto(Member member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setMemberCode(member.getMemberCode());
        dto.setFirstName(member.getFirstName());
        dto.setLastName(member.getLastName());
        dto.setEmail(member.getEmail());
        dto.setPhone(member.getPhone());
        dto.setDateOfBirth(member.getDateOfBirth());
        dto.setGender(member.getGender());
        dto.setAddress(member.getAddress());
        dto.setCity(member.getCity());
        dto.setState(member.getState());
        dto.setPincode(member.getPincode());
        dto.setEmergencyContactName(member.getEmergencyContactName());
        dto.setEmergencyContactPhone(member.getEmergencyContactPhone());
        dto.setEmergencyContactRelation(member.getEmergencyContactRelation());
        dto.setMedicalConditions(member.getMedicalConditions());
        dto.setAllergies(member.getAllergies());
        dto.setFitnessGoals(member.getFitnessGoals());
        dto.setProfileImage(member.getProfileImage());
        dto.setStatus(member.getStatus());
        dto.setJoinDate(member.getJoinDate());
        dto.setCreatedAt(member.getCreatedAt());
        dto.setUpdatedAt(member.getUpdatedAt());
        return dto;
    }
}