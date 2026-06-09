package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_modules", uniqueConstraints = {
    @UniqueConstraint(name = "uk_account_module", columnNames = {"account_id", "module_type"})
})
@Data
public class AccountModule {
    @Id
    @Column(length = 32)
    private String id;
    
    @Column(name = "account_id", nullable = false, length = 32)
    private String accountId;
    
    @Column(name = "module_type", nullable = false, length = 20)
    private String moduleType;
    
    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;
    
    @Column(name = "icon_url", nullable = false, length = 255)
    private String iconUrl;
    
    @Column(name = "can_pay", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Integer canPay = 0;
    
    @Column(name = "sort_order", columnDefinition = "int default 0")
    private Integer sortOrder = 0;
    
    @Column(name = "is_enabled", nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer isEnabled = 1;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
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