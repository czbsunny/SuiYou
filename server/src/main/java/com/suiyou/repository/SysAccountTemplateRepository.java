package com.suiyou.repository;

import com.suiyou.model.SysAccountTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAccountTemplateRepository extends JpaRepository<SysAccountTemplate, Long> {
    List<SysAccountTemplate> findByInstCode(String instCode);
    List<SysAccountTemplate> findByInstCodeAndAccountType(String instCode, String accountType);
    List<SysAccountTemplate> findByInstCodeAndEnabledTrue(String instCode);
    List<SysAccountTemplate> findByInstCodeAndAccountTypeAndEnabledTrue(String instCode, String accountType);
    void deleteByInstCode(String instCode);
}