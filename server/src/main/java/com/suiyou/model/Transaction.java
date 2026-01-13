package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.suiyou.model.enums.TransactionType;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 归属与权限
    @Column(name = "family_id", nullable = false)
    private Long familyId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "visible_scope", nullable = false, columnDefinition = "varchar(20) default 'PRIVATE'")
    private String visibleScope = "PRIVATE";
    
    // 核心交易要素
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "trans_time", nullable = false)
    private LocalDateTime transTime;
    
    // 资金流向 - 核心复式记账逻辑
    @Column(name = "source_asset_id")
    private Long sourceAssetId;
    
    @Column(name = "target_asset_id")
    private Long targetAssetId;
    
    @Column(nullable = false, columnDefinition = "decimal(19,4)")
    private BigDecimal amount;

    @Column(name = "target_amount", precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal fee;

    /**
     * 消费分类 ID
     * 仅 EXPENSE/INCOME 有效，转账通常不需要此分类
     */
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "tags", columnDefinition = "json")
    private String tags;

    @Column(name = "location", length = 255)
    private String location;

    /**
     * 状态: NORMAL(正常), DELETED(逻辑删除), PENDING(待确认/信用卡预授权)
     */
    @Column(nullable = false, length = 20)
    private String status = "NORMAL";
    
    // 时间戳
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
