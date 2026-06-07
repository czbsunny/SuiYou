package com.suiyou.repository;

import com.suiyou.model.AccountModule;
import com.suiyou.model.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountModuleRepository extends JpaRepository<AccountModule, Long> {
    
    List<AccountModule> findByAccountIdAndStatus(Long accountId, Integer status);
    
    List<AccountModule> findByAccountId(Long accountId);
    
    Optional<AccountModule> findByIdAndAccountId(Long id, Long accountId);
    
    Optional<AccountModule> findByAccountIdAndAssetType(Long accountId, AssetType assetType);
    
    boolean existsByAccountIdAndAssetType(Long accountId, AssetType assetType);
}