package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_snapshots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_snapshot_date", columnNames = {"owner_id", "snapshot_date"})
})
@Data
public class AssetSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;
    
    // 1. 流动资产
    @Column(name = "liquid_assets", nullable = false, columnDefinition = "decimal(18,2) default '0.00'", precision = 18, scale = 2)
    private BigDecimal liquidAssets = BigDecimal.ZERO;
    
    // 2. 投资资产
    @Column(name = "investment_assets", nullable = false, columnDefinition = "decimal(18,2) default '0.00'", precision = 18, scale = 2)
    private BigDecimal investmentAssets = BigDecimal.ZERO;
    
    // 3. 固定资产
    @Column(name = "fixed_assets", nullable = false, columnDefinition = "decimal(18,2) default '0.00'", precision = 18, scale = 2)
    private BigDecimal fixedAssets = BigDecimal.ZERO;
    
    // 4. 其他资产
    @Column(name = "other_assets", nullable = false, columnDefinition = "decimal(18,2) default '0.00'", precision = 18, scale = 2)
    private BigDecimal otherAssets = BigDecimal.ZERO;
    
    // 5. 总负债
    @Column(name = "total_liabilities", nullable = false, columnDefinition = "decimal(18,2) default '0.00'", precision = 18, scale = 2)
    private BigDecimal totalLiabilities = BigDecimal.ZERO;
    
    // 汇总数值
    @Column(name = "total_assets", nullable = false, columnDefinition = "decimal(18,2) default '0.00'", precision = 18, scale = 2)
    private BigDecimal totalAssets = BigDecimal.ZERO;
    
    @Column(name = "net_worth", nullable = false, columnDefinition = "decimal(18,2) default '0.00'", precision = 18, scale = 2)
    private BigDecimal netWorth = BigDecimal.ZERO;
    
    // 详情快照
    @Column(name = "details_snapshot", columnDefinition = "json")
    private String detailsSnapshot;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
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