package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
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
    private String institution;
    
    @Column(nullable = false)
    private String institutionIdentifier;
    
    private String accountName;
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean includeInNetWorth = true;

    @Column(nullable = false, columnDefinition = "varchar(20) default '#1F2937'")
    private String themeColor = "#1F2937";
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer status = 1;

    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 9999")
    private Integer sortOrder = 9999;
    
    @Column(nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean deleted = false;
    
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