package com.suiyou.repository;

import com.suiyou.model.AccountModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountModuleRepository extends JpaRepository<AccountModule, Long> {
    List<AccountModule> findByAccountIdAndEnabled(String accountId, Integer isEnabled);
}