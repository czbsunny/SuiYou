package com.suiyou.dto.es;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FundLatestResp {
    private String fundCode;
    private double latestNav;
    private LocalDate latestNavDate;
    private LocalDateTime navUpdatedAt;

    public SymoblLatestResp toSymoblLatestResp() {
        return new SymoblLatestResp(fundCode, latestNav, latestNavDate, navUpdatedAt);
    }
}
