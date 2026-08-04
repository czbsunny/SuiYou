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
    public HoldingResponse updateNetWorth(Long id, BigDecimal newTotalBalance, String strategyType) {
        Holding holding = holdingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found: " + id));
        
        HoldingStrategy strategy = strategyFactory.getStrategy(strategyType);
        Holding updatedHolding = strategy.updateNetWorth(holding, newTotalBalance);
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
    public List<HoldingResponse> getHoldingsByOwnerId(Long ownerId) {
        List<Holding> holdings = holdingRepository.findByOwnerId(ownerId);
        return holdings.stream()
                .map(this::toHoldingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteHolding(Long id) {
        if (!holdingRepository.existsById(id)) {
            throw new IllegalArgumentException("Holding not found: " + id);
        }
        holdingRepository.deleteById(id);
    }

    private HoldingResponse toHoldingResponse(Holding holding) {
        return HoldingResponse.builder()
                .id(holding.getId())
                .accountId(holding.getAccountId())
                .assetId(holding.getAssetId())
                .ownerId(holding.getOwnerId())
                .groupType(holding.getGroupType())
                .category(holding.getCategory())
                .subCategory(holding.getSubCategory())
                .name(holding.getName())
                .totalBalance(holding.getTotalBalance())
                .frozenBalance(holding.getFrozenBalance())
                .availableBalance(holding.getAvailableBalance())
                .currency(holding.getCurrency())
                .includeInNetWorth(holding.getIncludeInNetWorth())
                .valuationMode(holding.getValuationMode())
                .status(holding.getStatus())
                .attributes(holding.getAttributes())
                .createdAt(holding.getCreatedAt())
                .updatedAt(holding.getUpdatedAt())
                .build();
    }
}