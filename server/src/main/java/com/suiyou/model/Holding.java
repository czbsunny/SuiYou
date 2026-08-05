package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "holding")
@Data
@SQLRestriction("deleted_at IS NULL")
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "decimal(24,8) default '0.00000000'", precision = 24, scale = 8)
    private BigDecimal qty = BigDecimal.ZERO;

    @Column(nullable = false, columnDefinition = "decimal(19,4) default '1.0000'", precision = 19, scale = 4)
    private BigDecimal price = BigDecimal.ONE;

    @Column(nullable = false, columnDefinition = "decimal(19,4)", precision = 19, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "cost_basis", nullable = false, columnDefinition = "decimal(19,4)", precision = 19, scale = 4)
    private BigDecimal costBasis = BigDecimal.ZERO;

    @Column(name = "realized_pnl", nullable = false, columnDefinition = "decimal(19,4) default '0.0000'", precision = 19, scale = 4)
    private BigDecimal realizedPnl = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    private String side = "asset";

    @Column(nullable = false, length = 20)
    private String status = "active";

    @Column(name = "holding_type", nullable = false, length = 30)
    private String holdingType;

    @Column(name = "extra_attributes", columnDefinition = "json")
    private String extraAttributes;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.amount == null || this.amount.compareTo(BigDecimal.ZERO) == 0) {
            this.amount = this.qty.multiply(this.price);
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.amount = this.qty.multiply(this.price);
        updatedAt = LocalDateTime.now();
    }
}