package com.suiyou.dto.portfolio;

import com.suiyou.model.Portfolio;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PortfolioRespDTO {
    private Long id;
    private Long userId;
    private Long assetId;
    private String assetName;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double currentValue;
    private Double initialInvestment;
    private Double totalReturn;
    private Double totalReturnRate;
    private Double dailyReturn;
    private Double dailyReturnRate;
    private List<PortfolioHoldingRespDTO> items;

    public static PortfolioRespDTO fromEntity(Portfolio portfolio) {
        PortfolioRespDTO dto = new PortfolioRespDTO();
        dto.setId(portfolio.getId());
        dto.setUserId(portfolio.getUser() != null ? portfolio.getUser().getId() : null);
        dto.setAssetId(portfolio.getAsset() != null ? portfolio.getAsset().getId() : null);
        dto.setAssetName(portfolio.getAsset() != null ? portfolio.getAsset().getName() : null);
        dto.setName(portfolio.getName());
        dto.setDescription(portfolio.getDescription());
        dto.setCreatedAt(portfolio.getCreatedAt());
        dto.setUpdatedAt(portfolio.getUpdatedAt());
        dto.setCurrentValue(portfolio.getCurrentValue());
        dto.setInitialInvestment(portfolio.getInitialInvestment());
        dto.setTotalReturn(portfolio.getTotalReturn());
        dto.setTotalReturnRate(portfolio.getTotalReturnRate());
        dto.setDailyReturn(portfolio.getDailyReturn());
        dto.setDailyReturnRate(portfolio.getDailyReturnRate());
        if (portfolio.getHoldings() != null) {
            dto.setItems(portfolio.getHoldings().stream()
                    .map(PortfolioHoldingRespDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}