package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_goal_template", 
       uniqueConstraints = @UniqueConstraint(name = "uk_tpl_code", columnNames = "template_code"),
       indexes = @Index(name = "idx_category", columnList = "category_code"))
@Data
public class SysGoalTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_code", nullable = false, length = 32)
    private String categoryCode;
    
    @Column(name = "template_code", nullable = false, length = 64)
    private String templateCode;
    
    @Column(nullable = false, length = 128)
    private String name;
    
    @Column(name = "icon_url", length = 255)
    private String iconUrl;
    
    @Column(name = "default_amount", precision = 18, scale = 2)
    private BigDecimal defaultAmount;
    
    @Column(name = "default_period_days")
    private Integer defaultPeriodDays;
    
    @Column(name = "input_mode", nullable = false, length = 16, columnDefinition = "varchar(16) default 'SIMPLE'")
    private String inputMode = "SIMPLE";
    
    @Column(name = "wizard_config", columnDefinition = "json")
    private String wizardConfig;
    
    @Column(name = "milestone_strategy", nullable = false, length = 16, columnDefinition = "varchar(16) default 'DEFAULT'")
    private String milestoneStrategy = "DEFAULT";
    
    @Column(name = "milestone_config", columnDefinition = "json")
    private String milestoneConfig;
    
    @Column(name = "sort_order", columnDefinition = "int default 0")
    private Integer sortOrder = 0;
    
    @Column(name = "is_hot", columnDefinition = "tinyint default 0")
    private Integer isHot = 0;
    
    @Column(name = "created_at", columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}