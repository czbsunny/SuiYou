package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_account_asset", columnNames = {"account_id", "asset_type"})
})
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(name = "asset_type", nullable = false, length = 20)
    private String assetType;
    
    @Column(name = "asset_name", nullable = false, length = 100)
    private String assetName;
    
    @Column(name = "icon_url", nullable = false, length = 255)
    private String iconUrl;
    
    @Column(name = "bg_color", nullable = false, length = 10)
    private String bgColor;
    
    @Column(name = "can_pay", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Integer canPay = 0;
    
    @Column(name = "sort_order", columnDefinition = "int default 0")
    private Integer sortOrder = 0;
    
    @Column(name = "is_enabled", nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer isEnabled = 1;
    
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