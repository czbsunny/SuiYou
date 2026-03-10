package com.suiyou.service.impl;

import com.suiyou.model.Asset;
import com.suiyou.model.AssetPosition;
import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.AssetPositionRepository;
import com.suiyou.service.AssetPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetPositionServiceImpl implements AssetPositionService {

    @Autowired
    private AssetPositionRepository assetPositionRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Override
    public List<AssetPosition> getPositions(Long assetId) {
        return assetPositionRepository.findByAssetId(assetId);
    }

    @Override
    @Transactional
    public AssetPosition addPosition(Long assetId, String symbol, String name, BigDecimal quantity, BigDecimal cost) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        AssetPosition position = new AssetPosition();
        position.setAsset(asset);
        position.setSymbol(symbol);
        position.setName(name);
        position.setQuantity(quantity);
        position.setCost(cost);
        
        // 初始化计算字段
        position.setCurrentPrice(cost); // 初始时当前价格设为成本价
        position.setAmount(quantity.multiply(cost));
        position.setReturnValue(BigDecimal.ZERO);
        position.setReturnRate(BigDecimal.ZERO);
        
        return assetPositionRepository.save(position);
    }

    @Override
    @Transactional
    public AssetPosition updatePosition(Long positionId, BigDecimal quantity, BigDecimal cost) {
        AssetPosition position = assetPositionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        
        if (quantity != null) position.setQuantity(quantity);
        if (cost != null) position.setCost(cost);
        
        // 重新计算 Amount
        position.setAmount(position.getQuantity().multiply(position.getCurrentPrice()));
        
        return assetPositionRepository.save(position);
    }

    @Override
    @Transactional
    public void deletePosition(Long positionId) {
        assetPositionRepository.deleteById(positionId);
    }
}
