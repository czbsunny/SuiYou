package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_feature_usage",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "feature_code"})}
)
public class UserFeatureUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String featureCode;

    @Column(name = "daily_used", nullable = false)
    private Integer dailyUsed = 0;

    @Column(name = "monthly_used", nullable = true)
    private Integer monthlyUsed = 0;

    @Column(name = "total_used", nullable = false)
    private Integer totalUsed = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
