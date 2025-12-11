package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocation")
@Data
public class Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 归属与权限
    @Column(name = "family_id", nullable = false)
    private Long familyId;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "visible_scope", nullable = false, columnDefinition = "varchar(20) default 'PRIVATE'")
    private String visibleScope = "PRIVATE";
    
    // 核心分配要素
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(name = "goal_id", nullable = false)
    private Long goalId;
    
    @Column(nullable = false, columnDefinition = "decimal(19,4)")
    private BigDecimal amount;
    
    @Column(name = "allocation_time", nullable = false)
    private LocalDateTime allocationTime;
    
    // 快照/记录
    private String memo;
    
    // 时间戳
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
