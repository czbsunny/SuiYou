package com.suiyou.service.impl;

import com.suiyou.dto.holding.HoldingCreateRequest;
import com.suiyou.dto.holding.HoldingResponse;
import com.suiyou.dto.holding.HoldingUpdateRequest;
import com.suiyou.model.Holding;
import com.suiyou.repository.HoldingRepository;
import com.suiyou.strategy.HoldingStrategy;
import com.suiyou.strategy.HoldingStrategyFactory;
import com.suiyou.service.HoldingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HoldingServiceImpl implements HoldingService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private HoldingStrategyFactory strategyFactory;

    @Override
    @Transactional
    public HoldingResponse createHolding(HoldingCreateRequest request, String strategyType) {
        HoldingStrategy strategy = strategyFactory.getStrategy(strategyType);
        Holding holding = strategy.createHolding(request);
        Holding savedHolding = holdingRepository.save(holding);
        return toHoldingResponse(savedHolding);
    }

    @Override
    @Transactional
    public HoldingResponse updateBasicInfo(Long id, HoldingUpdateRequest request, String strategyType) {
        Holding holding = holdingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found: " + id));

        HoldingStrategy strategy = strategyFactory.getStrategy(strategyType);
        Holding updatedHolding = strategy.updateBasicInfo(holding, request);
        Holding savedHolding = holdingRepository.save(updatedHolding);
        return toHoldingResponse(savedHolding);
    }

    @Override
    @Transactional
    public HoldingResponse updatePrice(Long id, BigDecimal newPrice, String strategyType) {
        Holding holding = holdingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found: " + id));

        HoldingStrategy strategy = strategyFactory.getStrategy(strategyType);
        Holding updatedHolding = strategy.updatePrice(holding, newPrice);
        Holding savedHolding = holdingRepository.save(updatedHolding);
        return toHoldingResponse(savedHolding);
    }

    @Override
    public HoldingResponse getHoldingById(Long id) {
        Holding holding = holdingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found: " + id));
        return toHoldingResponse(holding);
    }

    @Override
    public List<HoldingResponse> getHoldingsByAccountId(Long accountId) {
        List<Holding> holdings = holdingRepository.findByAccountId(accountId);
        return holdings.stream()
                .map(this::toHoldingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<HoldingResponse> getHoldingsByAssetId(Long assetId) {
        List<Holding> holdings = holdingRepository.findByAssetId(assetId);
        return holdings.stream()
                .map(this::toHoldingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteHolding(Long id) {
        Holding holding = holdingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found: " + id));
        holding.setDeletedAt(LocalDateTime.now());
        holdingRepository.save(holding);
    }

    private HoldingResponse toHoldingResponse(Holding holding) {
        return HoldingResponse.builder()
                .id(holding.getId())
                .accountId(holding.getAccountId())
                .assetId(holding.getAssetId())
                .productId(holding.getProductId())
                .name(holding.getName())
                .qty(holding.getQty())
                .price(holding.getPrice())
                .amount(holding.getAmount())
                .costBasis(holding.getCostBasis())
                .realizedPnl(holding.getRealizedPnl())
                .side(holding.getSide())
                .status(holding.getStatus())
                .holdingType(holding.getHoldingType())
                .extraAttributes(holding.getExtraAttributes())
                .createdAt(holding.getCreatedAt())
                .updatedAt(holding.getUpdatedAt())
                .build();
    }
}