package com.suiyou.repository;

import com.suiyou.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByFamilyIdAndStatus(Long familyId, String status);
    List<Goal> findByCreatorId(Long creatorId);
    List<Goal> findByFamilyId(Long familyId);
}
