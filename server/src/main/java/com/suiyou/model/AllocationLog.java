package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocation_log")
@Data
public class AllocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long familyId;
    
    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long goalId;

    /**
     * 关联：如果是自动规则触发，记录 RuleID
     * 关联：如果是平替/打卡策略触发，记录 StrategyID
     */
    @Column(name = "rule_id", columnDefinition = "bigint DEFAULT NULL comment '关联规则ID (如果是自动触发)'")
    private Long ruleId;
    
    @Column(name = "strategy_id", columnDefinition = "bigint DEFAULT NULL comment '关联策略ID (如果是打卡/平替策略)'")
    private Long strategyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null comment '变动类型：ALLOCATE(分配), RELEASE(释放), CONSUME(消耗), SYNC(余额同步/盈亏)'")
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null comment '触发方式：MANUAL(手动), RULE(规则自动), STRATEGY(策略打卡)'")
    private TriggerType triggerType;

    @Column(nullable = false, precision = 19, scale = 4, columnDefinition = "decimal(19,4) not null comment '变动金额 (正数代表增加，负数代表减少)'")
    private BigDecimal amount;

    @Column(length = 255, columnDefinition = "varchar(255) comment '备注/快照'")
    private String memo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

// 变动类型枚举
enum ActionType {
    ALLOCATE, // 正向分配 (存入)
    RELEASE,  // 反向释放 (取出回账户)
    CONSUME,  // 消费 (达成目标花掉了)
    SYNC      // 同步 (因投资账户余额变动导致的盈亏调整)
}

// 触发方式枚举
enum TriggerType {
    MANUAL, // 手动触发
    RULE,   // 规则自动触发
    STRATEGY // 策略打卡触发
}