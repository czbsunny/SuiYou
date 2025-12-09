package com.suiyou.repository;

import com.suiyou.model.SysInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysInstitutionRepository extends JpaRepository<SysInstitution, Long> {
    // 根据机构代码查询
    SysInstitution findByInstCode(String instCode);
    
    // 根据机构类型查询
    List<SysInstitution> findByInstType(String instType);
    
    // 根据机构类型和排序顺序查询
    List<SysInstitution> findByInstTypeOrderBySortOrderAsc(String instType);
    
    // 查询所有机构并按排序顺序排列
    List<SysInstitution> findAllByOrderBySortOrderAsc();
}
