package com.suiyou.repository;

import com.suiyou.model.SysGoalCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysGoalCategoryRepository extends JpaRepository<SysGoalCategory, Long> {
    Optional<SysGoalCategory> findByCategoryCode(String categoryCode);
    List<SysGoalCategory> findByIsEnabledTrueOrderBySortOrderAsc();
}