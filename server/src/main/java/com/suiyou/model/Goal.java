package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal", indexes = {
    @Index(name = "idx_deadline", columnList = "deadline"),
    @Index(name = "idx_user_status", columnList = "user_id, status")
})
@Data
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 归属与权限
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "family_id", nullable = false)
    private Long familyId;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "visible_scope", nullable = false, columnDefinition = "varchar(20) default 'PRIVATE'")
    private String visibleScope = "PRIVATE";
    
    // 目标基本信息
    @Column(nullable = false, length = 128, name = "name")
    private String name;
    
    @Column(name = "target_amount", nullable = false, columnDefinition = "decimal(18,2)")
    private BigDecimal targetAmount;
    
    @Column(name = "deadline", columnDefinition = "date DEFAULT NULL")
    private LocalDate deadline;
    
    // 进度信息 (核心高频字段)
    @Column(name = "current_amount", nullable = false, columnDefinition = "decimal(18,2) default '0.00'")
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Column(name = "spent_amount", nullable = false, columnDefinition = "decimal(18,2) default '0.00'")
    private BigDecimal spentAmount = BigDecimal.ZERO;
    
    @Column(name = "progress_percent", nullable = false, columnDefinition = "decimal(5,2) default '0.00'")
    private BigDecimal progressPercent = BigDecimal.ZERO;
    
    // 状态管理
    @Column(name = "start_date", nullable = false, columnDefinition = "date")
    private LocalDate startDate;
    
    @Column(name = "status", nullable = false, length = 32, columnDefinition = "varchar(32) default 'ON_GOING'")
    private String status = "ON_GOING";
    
    @Column(name = "complete_date", columnDefinition = "date DEFAULT NULL")
    private LocalDate completeDate;
    
    // 扩展信息
    @Column(name = "wizard_result_snapshot", columnDefinition = "json DEFAULT NULL")
    private String wizardResultSnapshot;
    
    // 时间戳
    @Column(name = "created_at", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
