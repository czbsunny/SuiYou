package com.suiyou.repository;

import com.suiyou.model.GoalMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalMilestoneRepository extends JpaRepository<GoalMilestone, Long> {
    List<GoalMilestone> findByGoalIdOrderBySortOrderAsc(Long goalId);
    void deleteByGoalId(Long goalId);
}
