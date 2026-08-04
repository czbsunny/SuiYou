package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.suiyou.enums.TransactionType;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "family_id", nullable = false)
    private Long familyId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "visible_scope", nullable = false, columnDefinition = "varchar(20) default 'PRIVATE'")
    private String visibleScope = "PRIVATE";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(name = "trans_time", nullable = false)
    private LocalDateTime transTime;
    
    @Column(name = "source_holding_id")
    private Long sourceHoldingId;
    
    @Column(name = "target_holding_id")
    private Long targetHoldingId;
    
    @Column(nullable = false, columnDefinition = "decimal(19,4)")
    private BigDecimal amount;

    @Column(name = "target_amount", precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal fee;

    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "tags", columnDefinition = "json")
    private String tags;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "use_frozen_amount", nullable = false, columnDefinition = "bit(1) default 0")
    private Boolean useFrozenAmount = false;

    @Column(nullable = false, length = 20)
    private String status = "NORMAL";
    
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