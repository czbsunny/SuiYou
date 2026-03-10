package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@ToString(exclude = {"asset"})
@Table(name = "asset_position", indexes = {
    @Index(name = "idx_symbol", columnList = "symbol")
})
public class AssetPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Asset asset;

    @Column(nullable = false, length = 50)
    private String symbol; // 基金或股票代码

    @Column(length = 100)
    private String name; // 基金名称

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity; // 持有份额/股数

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal cost; // 单位成本价

    @Column(name = "current_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentPrice = BigDecimal.ZERO; // 当前价格

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO; // 持有金额 (quantity * currentPrice)

    @Column(name = "return_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal returnValue = BigDecimal.ZERO; // 持有收益

    @Column(name = "return_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal returnRate = BigDecimal.ZERO; // 收益率

    @Column(name = "daily_return", nullable = false, precision = 18, scale = 2)
    private BigDecimal dailyReturn = BigDecimal.ZERO; // 当日收益

    @Column(name = "daily_return_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal dailyReturnRate = BigDecimal.ZERO; // 当日收益率

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "nav_updated_at", nullable = true)
    private LocalDateTime navUpdatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
