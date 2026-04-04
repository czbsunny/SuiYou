package com.suiyou.dto.portfolio;

import java.util.List;

import lombok.Data;

@Data
public class UpdatePortfolioHoldingsDTO {
    List<CreatePortfolioHoldingDTO> holdings;
}
