package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "family_daily_snapshots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_family_date", columnNames = {"familyId", "date"})
})
@Data
public class FamilyDailySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "family_id", nullable = false)
    private Long familyId;
    
    @Column(nullable = false)
    private LocalDate date;
    
    // 基准信息
    @Column(name = "base_currency", nullable = false, columnDefinition = "varchar(10) default 'CNY'")
    private String baseCurrency = "CNY";
    
    // 资产端 (Assets) - 存正数
    @Column(name = "asset_liquid", nullable = false, columnDefinition = "decimal(19,4) default 0")
    private BigDecimal assetLiquid = BigDecimal.ZERO;
    
    @Column(name = "asset_invest", nullable = false, columnDefinition = "decimal(19,4) default 0")
    private BigDecimal assetInvest = BigDecimal.ZERO;
    
    @Column(name = "asset_fixed", nullable = false, columnDefinition = "decimal(19,4) default 0")
    private BigDecimal assetFixed = BigDecimal.ZERO;
    
    @Column(name = "asset_other", nullable = false, columnDefinition = "decimal(19,4) default 0")
    private BigDecimal assetOther = BigDecimal.ZERO;
    
    // 负债端 (Liabilities) - 建议存负数
    // 存负数的好处：计算净值直接 SUM 所有字段，不用做减法，代码逻辑统一
    @Column(nullable = false, columnDefinition = "decimal(19,4) default 0")
    private BigDecimal liabilities = BigDecimal.ZERO;
    
    // 计算字段 - 净值 = 资产总和 - 负债绝对值
    // 如果负债存负数，则净值 = 资产总和 + 负债
    @Transient
    public BigDecimal getNetWorth() {
        return assetLiquid.add(assetInvest).add(assetFixed).add(assetOther).add(liabilities);
    }
    
    // 计算字段 - 总资产
    @Transient
    public BigDecimal getTotalAssets() {
        return assetLiquid.add(assetInvest).add(assetFixed).add(assetOther);
    }
}
