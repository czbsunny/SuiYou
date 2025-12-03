package com.suiyou.repository;

import com.suiyou.model.SysAssetCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAssetCategoriesRepository extends JpaRepository<SysAssetCategories, Long> {
    // 根据分类代码查询
    SysAssetCategories findByCode(String code);
    
    // 根据父级代码查询所有子分类
    List<SysAssetCategories> findByParentCode(String parentCode);
    
    // 根据分组类型查询
    List<SysAssetCategories> findByGroupType(String groupType);
}
