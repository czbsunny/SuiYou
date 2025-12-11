package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_milestone")
@Data
public class GoalMilestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "target_amount", nullable = false, columnDefinition = "decimal(19,4)")
    private BigDecimal targetAmount;
    
    @Column(name = "is_reached", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean isReached = false;
    
    @Column(name = "reached_at")
    private LocalDateTime reachedAt;
    
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;
    
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    // 外键关系
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "goal_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Goal goal;
}
