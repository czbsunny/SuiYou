package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocation_rule")
@Data
public class AllocationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long familyId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long goalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null comment '规则类型：BINDING(整卡绑定), SCHEDULE(定期), RATIO_INCOME(按比例)'")
    private RuleType type;

    // --- 规则参数区域 (根据Type不同，取不同字段) ---

    @Column(precision = 19, scale = 4, columnDefinition = "decimal(19,4) comment '固定金额 (用于 SCHEDULE 定期分配)'")
    private BigDecimal fixedAmount;

    @Column(precision = 5, scale = 4, columnDefinition = "decimal(5,4) comment '比例因子 (用于 RATIO_INCOME，如 0.10 代表 10%)'")
    private BigDecimal ratio;

    @Column(length = 64, columnDefinition = "varchar(64) comment 'Cron表达式 (用于 SCHEDULE，如 \'0 0 1 * * ?\')'")
    private String cronExpression;

    // --- 状态控制 ---

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true comment '是否启用'")
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// 规则类型枚举
enum RuleType {
    BINDING,      // 账户余额 = 目标余额 (强一致)
    SCHEDULE,     // 定时任务 (每月存多少)
    RATIO_INCOME  // 监听收入流水 (按收入比例存)
}