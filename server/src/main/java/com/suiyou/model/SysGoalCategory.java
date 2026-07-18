package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_goal_category")
@Data
public class SysGoalCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_code", nullable = false, unique = true, length = 32)
    private String categoryCode;
    
    @Column(name = "name", nullable = false, length = 64)
    private String name;
    
    @Column(name = "slogan", length = 128)
    private String slogan;
    
    @Column(name = "icon_url", length = 255)
    private String iconUrl;
    
    @Column(name = "bg_color", length = 32)
    private String bgColor;
    
    @Column(name = "sort_order", columnDefinition = "int unsigned default 0")
    private Integer sortOrder = 0;
    
    @Column(name = "is_enabled", columnDefinition = "tinyint unsigned default 1")
    private Boolean isEnabled = true;
    
    @Column(name = "created_at", updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
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