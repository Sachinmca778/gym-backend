package com.example.gym.backend.repository;

import com.example.gym.backend.entity.MemberMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MemberMembershipRepository extends JpaRepository<MemberMembership, Long> {

    List<MemberMembership> findByMemberId(Long memberId);
    List<MemberMembership> findByStatus(MemberMembership.MembershipStatus status);

    @Query("SELECT m FROM MemberMembership m WHERE m.member.id = :memberId AND m.status = 'ACTIVE'")
    List<MemberMembership> findActiveMembershipsByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT m FROM MemberMembership m WHERE m.endDate <= :date AND m.status = 'ACTIVE'")
    List<MemberMembership> findExpiringMemberships(@Param("date") LocalDate date);
}