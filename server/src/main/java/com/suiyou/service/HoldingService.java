package com.suiyou.service;

import com.suiyou.dto.holding.HoldingCreateRequest;
import com.suiyou.dto.holding.HoldingResponse;
import com.suiyou.dto.holding.HoldingUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

public interface HoldingService {
    HoldingResponse createHolding(HoldingCreateRequest request, String strategyType);
    
    HoldingResponse updateBasicInfo(Long id, HoldingUpdateRequest request, String strategyType);
    
    HoldingResponse updateNetWorth(Long id, BigDecimal newTotalBalance, String strategyType);
    
    HoldingResponse getHoldingById(Long id);
    
    List<HoldingResponse> getHoldingsByAccountId(Long accountId);
    
    List<HoldingResponse> getHoldingsByOwnerId(Long ownerId);
    
    void deleteHolding(Long id);
}