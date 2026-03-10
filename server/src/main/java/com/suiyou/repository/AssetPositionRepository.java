package com.suiyou.repository;

import com.suiyou.model.AssetPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetPositionRepository extends JpaRepository<AssetPosition, Long> {
    List<AssetPosition> findByAssetId(Long assetId);
    List<AssetPosition> findBySymbol(String symbol);
    void deleteByAssetId(Long assetId);
}
