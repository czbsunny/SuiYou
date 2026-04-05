package com.suiyou.dto.portfolio;

import lombok.Data;
import com.suiyou.model.enums.PortfolioType;

@Data
public class CreatePortfolioDTO {
    private Long assetId;
    private String name;
    private String description;
    private PortfolioType type;
}