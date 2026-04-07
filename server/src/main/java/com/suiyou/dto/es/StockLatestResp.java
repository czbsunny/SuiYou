package com.suiyou.dto.es;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockLatestResp {
    private String stockCode;
    private double latestNav;
    private LocalDate latestNavDate;
    private LocalDateTime navUpdatedAt;

    public SymoblLatestResp toSymoblLatestResp() {
        return new SymoblLatestResp(stockCode, latestNav, latestNavDate, navUpdatedAt);
    }
}
