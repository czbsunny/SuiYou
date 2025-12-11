package com.suiyou.repository;

import com.suiyou.model.SysGoalCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysGoalCategoryRepository extends JpaRepository<SysGoalCategory, Long> {
    // 根据分类编码查询
    SysGoalCategory findByCategoryCode(String categoryCode);
    
    // 查询所有启用的分类
    List<SysGoalCategory> findAllByIsEnabled(boolean isEnabled);
    
    // 按排序权重升序查询所有分类
    List<SysGoalCategory> findAllByOrderBySortOrderAsc();
    
    // 按排序权重升序查询启用的分类
    List<SysGoalCategory> findAllByIsEnabledOrderBySortOrderAsc(boolean isEnabled);
    
    // 根据编码列表批量查询
    List<SysGoalCategory> findByCategoryCodeIn(List<String> categoryCodes);
    
    // 根据编码列表批量删除
    void deleteByCategoryCodeIn(List<String> categoryCodes);
}