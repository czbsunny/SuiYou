package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feature_limits")
public class FeatureLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String featureCode;

    @Column(nullable = false, length = 50)
    private String planCode;

    @Column(name = "daily_limit", nullable = false)
    private Integer dailyLimit;

    @Column(name = "monthly_limit", nullable = true)
    private Integer monthlyLimit;

    @Column(name = "total_limit", nullable = true)
    private Integer totalLimit;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
