package com.suiyou.repository;

import com.suiyou.model.TimeDepositItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeDepositItemRepository extends JpaRepository<TimeDepositItem, Long> {

    List<TimeDepositItem> findByAssetId(Long assetId);

    List<TimeDepositItem> findByAssetIdAndStatus(Long assetId, Integer status);

    Optional<TimeDepositItem> findByIdAndStatus(Long id, Integer status);

    List<TimeDepositItem> findByAssetIdIn(List<Long> assetIds);

    List<TimeDepositItem> findByAssetIdInAndStatus(List<Long> assetIds, Integer status);

    void deleteByAssetId(Long assetId);
}