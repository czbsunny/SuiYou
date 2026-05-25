package com.suiyou.repository;

import com.suiyou.model.Asset;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    List<Asset> findByAccountModuleId(Long accountModuleId);
    
    List<Asset> findByAccountModuleIdAndStatus(Long accountModuleId, Integer status);
    
    List<Asset> findByAccountModuleIdAndCategory(Long accountModuleId, String category);
    
    List<Asset> findByAccountModule_AccountId(Long accountId);
    
    List<Asset> findByAccountModule_AccountIdAndStatus(Long accountId, Integer status);
    
    List<Asset> findByAccountModule_AccountIdAndCategory(Long accountId, String category);
    
    List<Asset> findByAccountModule_AccountIdAndGroupType(Long accountId, String groupType);
    
    List<Asset> findByOwnerIdAndStatus(Long userId, Integer status);
    
    @Query("SELECT a FROM Asset a LEFT JOIN FETCH a.accountModule WHERE a.id = :id")
    Optional<Asset> findByIdWithAccountModule(Long id);
}