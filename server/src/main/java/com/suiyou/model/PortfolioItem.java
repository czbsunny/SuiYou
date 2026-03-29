package com.suiyou.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString(exclude = {"portfolio"})
@Table(name = "portfolio_items")
public class PortfolioItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 50)
    private String symbol;  // 基金或股票代码

    @Column(length = 100)
    private String name;    // 基金名称

    @Column(nullable = false)
    private Double quantity;  // 持有份额/股数

    @Column(nullable = false)
    private Double cost;  // 单位成本价

    @Column(name = "amount", nullable = false)
    private Double amount = 0.0;  // 持有金额

    @Column(name = "return_value", nullable = false)
    private Double returnValue = 0.0;  // 持有收益

    @Column(name = "return_rate", nullable = false)
    private Double returnRate = 0.0;  // 收益率

    @Column(name = "daily_return", nullable = false)
    private Double dailyReturn = 0.0;

    @Column(name = "daily_return_rate", nullable = false)
    private Double dailyReturnRate = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "nav_updated_at", nullable = false)
    private LocalDateTime navUpdatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}