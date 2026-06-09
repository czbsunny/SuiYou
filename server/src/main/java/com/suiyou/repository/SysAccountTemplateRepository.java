package com.suiyou.repository;

import com.suiyou.model.SysAccountTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAccountTemplateRepository extends JpaRepository<SysAccountTemplate, Long> {
    List<SysAccountTemplate> findByInstCodeAndAccountType(String instCode, String accountType);
    SysAccountTemplate findByInstCodeAndAccountTypeAndModuleType(String instCode, String accountType, String moduleType);

    void deleteByInstCode(String instCode);
}