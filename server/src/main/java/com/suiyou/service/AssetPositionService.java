package com.suiyou.service;

import com.suiyou.model.AssetPosition;
import java.math.BigDecimal;
import java.util.List;

public interface AssetPositionService {
    List<AssetPosition> getPositions(Long assetId);
    AssetPosition addPosition(Long assetId, String symbol, String name, BigDecimal quantity, BigDecimal cost);
    AssetPosition updatePosition(Long positionId, BigDecimal quantity, BigDecimal cost);
    void deletePosition(Long positionId);
}
