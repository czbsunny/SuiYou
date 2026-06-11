package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset")
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_module_id", nullable = false, length = 32)
    private String accountModuleId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(nullable = false)
    private String groupType;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String subCategory;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "total_balance", nullable = false, columnDefinition = "decimal(26,8) default '0.00000000'", precision = 26, scale = 8)
    private BigDecimal totalBalance = BigDecimal.ZERO;
    
    @Column(name = "frozen_balance", nullable = false, columnDefinition = "decimal(26,8) default '0.00000000'", precision = 26, scale = 8)
    private BigDecimal frozenBalance = BigDecimal.ZERO;
    
    @Column(name = "available_balance", nullable = false, columnDefinition = "decimal(26,8) default '0.00000000'", precision = 26, scale = 8)
    private BigDecimal availableBalance = BigDecimal.ZERO;
    
    @Column(nullable = false, columnDefinition = "varchar(10) default 'CNY'")
    private String currency = "CNY";
    
    @Column(nullable = false, columnDefinition = "bit(1) default 1")
    private Boolean includeInNetWorth = true;
    
    @Column(name = "valuation_mode", nullable = false, columnDefinition = "varchar(20) default 'MANUAL'")
    private String valuationMode = "MANUAL";
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer status = 1;

    @Column(columnDefinition = "json")
    private String attributes;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.availableBalance = this.totalBalance.subtract(this.frozenBalance);
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.availableBalance = this.totalBalance.subtract(this.frozenBalance);
        updatedAt = LocalDateTime.now();
    }
}