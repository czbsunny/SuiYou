package com.suiyou.repository;

import com.suiyou.model.SysCategoryInstitutionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysCategoryInstitutionRelationRepository extends JpaRepository<SysCategoryInstitutionRelation, Long> {
    // 根据机构编码查询关联的分类
    List<SysCategoryInstitutionRelation> findByInstCode(String instCode);
    
    // 根据分类编码查询关联的机构
    List<SysCategoryInstitutionRelation> findByCategoryCode(String categoryCode);
    
    // 根据机构编码和分类编码查询关联关系
    boolean existsByInstCodeAndCategoryCode(String instCode, String categoryCode);
}
