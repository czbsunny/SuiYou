package com.suiyou.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suiyou.model.SysCategory;

@Repository
public interface SysCategoryRepository extends JpaRepository<SysCategory, Long> {
    // 根据分类代码查询
    SysCategory findByCategoryCode(String categoryCode);
    
    // 根据父级代码查询所有子分类
    List<SysCategory> findByParentCode(String parentCode);
    
    // 查询所有系统分类
    List<SysCategory> findAllByIsSystem(boolean isSystem);
        
    // 根据分类代码列表批量删除
    void deleteByCategoryCodeIn(List<String> categoryCodes);
}
