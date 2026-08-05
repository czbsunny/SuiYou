package com.suiyou.service;

import com.suiyou.dto.holding.HoldingCreateRequest;
import com.suiyou.dto.holding.HoldingResponse;
import com.suiyou.dto.holding.HoldingUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

public interface HoldingService {
    HoldingResponse createHolding(HoldingCreateRequest request, String strategyType);

    HoldingResponse updateBasicInfo(Long id, HoldingUpdateRequest request, String strategyType);

    HoldingResponse updatePrice(Long id, BigDecimal newPrice, String strategyType);

    HoldingResponse getHoldingById(Long id);

    List<HoldingResponse> getHoldingsByAccountId(Long accountId);

    List<HoldingResponse> getHoldingsByAssetId(Long assetId);

    void deleteHolding(Long id);
}