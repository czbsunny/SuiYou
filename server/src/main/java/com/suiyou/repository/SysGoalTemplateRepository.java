package com.suiyou.repository;

import com.suiyou.model.SysGoalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysGoalTemplateRepository extends JpaRepository<SysGoalTemplate, Long> {
    
    List<SysGoalTemplate> findByCategoryCodeOrderBySortOrderAsc(String categoryCode);

    Optional<SysGoalTemplate> findByCategoryCode(String categoryCode);
    
    // 根据模板编码查询
    Optional<SysGoalTemplate> findByTemplateCode(String templateCode);
    
    // 根据模板编码列表批量查询
    List<SysGoalTemplate> findByTemplateCodeIn(List<String> templateCodes);
    
    // 根据模板编码列表批量删除
    void deleteByTemplateCodeIn(List<String> templateCodes);
}
