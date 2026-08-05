package com.suiyou.repository;

import com.suiyou.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByAccountId(Long accountId);

    List<Holding> findByAssetId(Long assetId);

    List<Holding> findByAccountIdAndStatus(Long accountId, String status);

    List<Holding> findByAssetIdAndStatus(Long assetId, String status);

    List<Holding> findByHoldingType(String holdingType);

    List<Holding> findByProductId(Long productId);

    Optional<Holding> findByIdAndAccountId(Long id, Long accountId);
}