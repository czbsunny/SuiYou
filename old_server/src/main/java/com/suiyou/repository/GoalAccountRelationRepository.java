package com.suiyou.repository;

import com.suiyou.model.GoalAccountRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalAccountRelationRepository extends JpaRepository<GoalAccountRelation, Long> {
    List<GoalAccountRelation> findByGoalId(Long goalId);
    List<GoalAccountRelation> findByAccountId(Long accountId);
    void deleteByGoalIdAndAccountId(Long goalId, Long accountId);
}
