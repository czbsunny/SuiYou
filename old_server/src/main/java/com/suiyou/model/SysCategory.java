package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_category")
@Data
public class SysCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    private String categoryCode;
    
    @Column(name = "parent_code", length = 50)
    private String parentCode;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "icon_url", length = 255)
    private String iconUrl;
    
    @Column(name = "description", length = 1023)
    private String description;
    
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;
    
    @Column(name = "group_type", nullable = false, length = 20)
    private String groupType;

    @Column(name = "is_system", columnDefinition = "tinyint(1) default 1")
    private Boolean isSystem = true;
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
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