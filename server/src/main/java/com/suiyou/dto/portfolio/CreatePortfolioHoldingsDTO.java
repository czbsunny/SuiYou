package com.suiyou.dto.portfolio;

import java.util.List;

import lombok.Data;

@Data
public class CreatePortfolioHoldingsDTO {
    List<CreatePortfolioHoldingDTO> holdings;
}
