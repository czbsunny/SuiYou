package com.suiyou.repository;

import com.suiyou.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByOwnerId(Long ownerId);
    
    List<Holding> findByAccountId(Long accountId);
    
    List<Holding> findByAssetId(Long assetId);
    
    Optional<Holding> findByIdAndOwnerId(Long id, Long ownerId);
    
    List<Holding> findByAccountIdAndStatus(Long accountId, Integer status);
    
    List<Holding> findByAssetIdAndStatus(Long assetId, Integer status);
    
    List<Holding> findByAssetIdAndCategory(Long assetId, String category);
}