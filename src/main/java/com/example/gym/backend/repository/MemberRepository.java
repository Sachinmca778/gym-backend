package com.example.gym.backend.repository;

import com.example.gym.backend.entity.Member;
import com.example.gym.backend.entity.Member.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberCode(String memberCode);
    Optional<Member> findByUserId(Long userId);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByPhone(String phone);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByMemberCode(String memberCode);

    List<Member> findByStatus(MemberStatus status);

    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.joinDate >= :startDate")
    List<Member> findActiveMembersJoinedAfter(@Param("status") MemberStatus status,
                                              @Param("startDate") LocalDate startDate);

    @Query("SELECT m FROM Member m WHERE " +
            "LOWER(m.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "m.phone LIKE CONCAT('%', :searchTerm, '%')")
    Page<Member> searchMembers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status")
    long countByStatus(@Param("status") MemberStatus status);

    @Query("SELECT DISTINCT m FROM Member m JOIN m.memberships mm WHERE mm.endDate <= :expiryDate AND m.status = 'ACTIVE'")
    List<Member> findMembersWithExpiringMemberships(@Param("expiryDate") LocalDate expiryDate);
}