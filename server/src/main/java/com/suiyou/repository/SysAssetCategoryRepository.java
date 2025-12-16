package com.suiyou.repository;

import com.suiyou.model.SysAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAssetCategoryRepository extends JpaRepository<SysAssetCategory, Long> {
    // 根据分类代码查询
    SysAssetCategory findByCategoryCode(String categoryCode);
    
    // 根据父级代码查询所有子分类
    List<SysAssetCategory> findByParentCode(String parentCode);
    
    // 根据分组类型查询
    List<SysAssetCategory> findByGroupType(String groupType);
    
    // 查询所有系统分类
    List<SysAssetCategory> findAllByIsSystem(boolean isSystem);
        
    // 根据分类代码列表批量删除
    void deleteByCategoryCodeIn(List<String> categoryCodes);
}
