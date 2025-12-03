package com.suiyou.repository;

import com.suiyou.model.SysGoalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysGoalTemplateRepository extends JpaRepository<SysGoalTemplate, Long> {
    List<SysGoalTemplate> findByCategoryOrderBySortOrderAsc(String category);
    Optional<SysGoalTemplate> findBySubCategory(String subCategory);
}
