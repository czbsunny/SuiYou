
package com.suiyou.repository;

import com.suiyou.model.SysInstitutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysInstitutionTypeRepository extends JpaRepository<SysInstitutionType, Long> {
    Optional<SysInstitutionType> findByTypeCode(String typeCode);
    List<SysInstitutionType> findAllByOrderBySortOrderAsc();
}
