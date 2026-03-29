package com.suiyou.dto.portfolio;

import com.suiyou.model.PortfolioItem;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortfolioItemRespDTO {
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

    public static PortfolioItemRespDTO fromEntity(PortfolioItem item) {
        PortfolioItemRespDTO dto = new PortfolioItemRespDTO();
        dto.setId(item.getId());
        dto.setPortfolioId(item.getPortfolio() != null ? item.getPortfolio().getId() : null);
        dto.setSymbol(item.getSymbol());
        dto.setName(item.getName());
        dto.setQuantity(item.getQuantity());
        dto.setCost(item.getCost());
        dto.setAmount(item.getAmount());
        dto.setReturnValue(item.getReturnValue());
        dto.setReturnRate(item.getReturnRate());
        dto.setDailyReturn(item.getDailyReturn());
        dto.setDailyReturnRate(item.getDailyReturnRate());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}