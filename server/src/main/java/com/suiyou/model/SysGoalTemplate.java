package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sys_goal_templates")
@Data
public class SysGoalTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Level 1: 大类
    @Column(nullable = false)
    private String category;
    
    // Level 2: 子项代码
    @Column(name = "sub_category", nullable = false, unique = true)
    private String subCategory;
    
    // UI 展示
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String icon;
    
    @Column(name = "color_hex")
    private String colorHex;
    
    // 预设值
    @Column(name = "default_horizon", columnDefinition = "varchar(20) default 'SHORT'")
    private String defaultHorizon = "SHORT";
    
    @Column(name = "sort_order", columnDefinition = "int default 0")
    private Integer sortOrder = 0;
}
