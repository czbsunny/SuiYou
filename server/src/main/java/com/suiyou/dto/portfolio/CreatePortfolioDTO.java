package com.suiyou.dto.portfolio;

import lombok.Data;

@Data
public class CreatePortfolioDTO {
    private Long assetId;
    private String name;
    private String description;
}