package com.suiyou.repository;

import com.suiyou.model.SysInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysInstitutionRepository extends JpaRepository<SysInstitution, Long> {
    SysInstitution findByInstCode(String instCode);
    List<SysInstitution> findByInstType(String instType);
    List<SysInstitution> findByInstTypeOrderBySortOrderAsc(String instType);
    List<SysInstitution> findAllByOrderBySortOrderAsc();
    List<SysInstitution> findByIsHotTrueOrderBySortOrderAsc();
}