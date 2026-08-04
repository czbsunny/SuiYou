package com.suiyou.repository;

import com.suiyou.model.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByFamilyIdAndStatus(Long familyId, Integer status);

    List<FamilyMember> findByUserIdAndStatus(Long userId, Integer status);

    Optional<FamilyMember> findByFamilyIdAndUserId(Long familyId, Long userId);

    boolean existsByFamilyIdAndUserId(Long familyId, Long userId);
}
