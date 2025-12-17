package com.suiyou.repository;

import com.suiyou.model.SysConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysConfigVersionRepository extends JpaRepository<SysConfigVersion, String> {
    
    Optional<SysConfigVersion> findByModuleKey(String moduleKey);

    List<SysConfigVersion> findAllByOrderByModuleKeyAsc();
}
