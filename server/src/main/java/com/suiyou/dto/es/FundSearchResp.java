package com.suiyou.dto.es;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FundSearchResp {
    private boolean success;
    private long total;
    private int page;
    private int pageSize;
    private List<FundSearchResult> funds;

    @Data
    public static class FundSearchResult {
        private String fundCode;
        private String name;
        private String fundType;
        private String company;
        private String manager;
        private float score;
        private Float latestNav;  // 最新净值
        private LocalDate latestNavDate;  // 最新净值日期
        private Float dailyGrowth;  // 日涨跌幅
        private LocalDateTime navUpdatedAt;  // 净值更新时间
    }
}