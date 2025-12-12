package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false, columnDefinition = "decimal(19,4)")
    private BigDecimal amount;
    
    @Column(name = "trans_time", nullable = false)
    private LocalDateTime transTime;
    
    // 资金流向 - 核心复式记账逻辑
    @Column(name = "source_account_id")
    private Long sourceAccountId;
    
    @Column(name = "target_account_id")
    private Long targetAccountId;
    
    // 分类信息
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "tags", columnDefinition = "json")
    private String tags;

    @Column(name = "location", length = 255)
    private String location;
    
    // 时间戳
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
