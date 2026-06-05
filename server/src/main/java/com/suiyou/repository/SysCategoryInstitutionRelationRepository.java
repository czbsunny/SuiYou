package com.suiyou.repository;

import com.suiyou.model.SysCategoryInstitutionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysCategoryInstitutionRelationRepository extends JpaRepository<SysCategoryInstitutionRelation, Long> {
    List<SysCategoryInstitutionRelation> findByInstCode(String instCode);
    
    List<SysCategoryInstitutionRelation> findByCategoryCode(String categoryCode);
    
    boolean existsByInstCodeAndCategoryCode(String instCode, String categoryCode);
    
    void deleteByCategoryCodeIn(List<String> categoryCodes);
    
    List<SysCategoryInstitutionRelation> findByInstCodeAndRelationType(String instCode, String relationType);
    
    List<SysCategoryInstitutionRelation> findByInstCodeOrderByRelationType(String instCode);
    
    void deleteAllByInstCode(String instCode);
}
