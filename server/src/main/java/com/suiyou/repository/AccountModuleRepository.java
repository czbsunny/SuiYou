package com.suiyou.repository;

import com.suiyou.model.AccountModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountModuleRepository extends JpaRepository<AccountModule, Long> {
    /**
     * 根据账户ID查询账户关联的模块
     * @param accountId 账户ID
     * @return 账户关联的模块列表
     */
    List<AccountModule> findByAccountId(Long accountId);
    
    /**
     * 根据账户ID和是否启用查询账户关联的模块
     * @param accountId 账户ID
     * @param isEnabled 是否启用
     * @return 账户关联的模块列表
     */
    List<AccountModule> findByAccountIdAndEnabled(Long accountId, Integer isEnabled);
}