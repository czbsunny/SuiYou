package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long familyId;
    
    @Column(nullable = false)
    private Long ownerId;
    
    @Column(nullable = false, columnDefinition = "varchar(20) default 'PRIVATE'")
    private String visibleScope = "PRIVATE";
    
    @Column(nullable = false)
    private String groupType;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String subCategory;
    
    private String institution;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, columnDefinition = "decimal(19,4) default '0.0000'")
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false, columnDefinition = "varchar(10) default 'CNY'")
    private String currency = "CNY";
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer includeInNetWorth = 1;
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer status = 1;
    
    @Column(columnDefinition = "json")
    private String attributes;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
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