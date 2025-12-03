package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
@Data
public class Goal {
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
    
    // 核心分类
    @Column(nullable = false)
    private String category;
    
    @Column(name = "sub_category", nullable = false)
    private String subCategory;
    
    // 展示信息
    @Column(nullable = false, length = 50)
    private String name;
    
    private String icon;
    
    private String color;
    
    // 金额管理
    @Column(name = "target_amount", nullable = false, columnDefinition = "decimal(19,4)")
    private BigDecimal targetAmount;
    
    // 进度快照
    @Column(name = "current_amount", nullable = false, columnDefinition = "decimal(19,4) default '0.0000'")
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    // 时间规划
    private String horizon = "SHORT";
    
    @Column(name = "start_date", nullable = false, columnDefinition = "date default CURRENT_DATE")
    private LocalDate startDate;
    
    private LocalDate deadline;
    
    // 状态管理
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer status = 1;
    
    // 时间戳
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
