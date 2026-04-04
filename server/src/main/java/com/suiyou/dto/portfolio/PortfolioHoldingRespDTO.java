package com.suiyou.dto.portfolio;

import com.suiyou.model.PortfolioHolding;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortfolioHoldingRespDTO {
    private Long id;
    private Long portfolioId;
    private String symbol;
    private String name;
    private Double quantity;
    private Double cost;
    private Double amount;
    private Double returnValue;
    private Double returnRate;
    private Double dailyReturn;
    private Double dailyReturnRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PortfolioHoldingRespDTO fromEntity(PortfolioHolding holding) {
        PortfolioHoldingRespDTO dto = new PortfolioHoldingRespDTO();
        dto.setId(holding.getId());
        dto.setPortfolioId(holding.getPortfolio() != null ? holding.getPortfolio().getId() : null);
        dto.setSymbol(holding.getSymbol());
        dto.setName(holding.getName());
        dto.setQuantity(holding.getQuantity());
        dto.setCost(holding.getCost());
        dto.setAmount(holding.getAmount());
        dto.setReturnValue(holding.getReturnValue());
        dto.setReturnRate(holding.getReturnRate());
        dto.setDailyReturn(holding.getDailyReturn());
        dto.setDailyReturnRate(holding.getDailyReturnRate());
        dto.setCreatedAt(holding.getCreatedAt());
        dto.setUpdatedAt(holding.getUpdatedAt());
        return dto;
    }
}