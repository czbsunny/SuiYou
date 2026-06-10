package com.suiyou.repository;

import com.suiyou.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByOwnerId(Long ownerId);
    
    List<Asset> findByAccountId(Long accountId);
    
    List<Asset> findByAccountModuleId(String accountModuleId);
    
    Optional<Asset> findByIdAndOwnerId(Long id, Long ownerId);
    
    List<Asset> findByAccountIdAndStatus(Long accountId, Integer status);
}