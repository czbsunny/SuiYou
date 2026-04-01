package com.suiyou.dto.es;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FundDocument {
    private String fundCode;
    private String name;
    private String fullName;
    private String fundType;
    private String company;
    private String manager;
    private LocalDate establishDate;
    private String riskLevel;
    private String rating;
    private Float latestNav;  // 最新净值
    private LocalDate latestNavDate;  // 最新净值日期
    private Float dailyGrowth;  // 日涨跌幅
    private LocalDateTime navUpdatedAt;  // 净值更新时间
}