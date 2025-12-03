package com.suiyou.repository;

import com.suiyou.model.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    // 按目标ID查询分配记录（核心查询：查某个目标现在攒了多少钱）
    List<Allocation> findByGoalId(Long goalId);
    
    // 按账户ID查询分配记录（辅助查询：查某个账户被分配到了哪些目标）
    List<Allocation> findByAccountId(Long accountId);
    
    // 按家庭ID查询分配记录
    List<Allocation> findByFamilyId(Long familyId);
    
    // 按目标ID和时间范围查询分配记录
    List<Allocation> findByGoalIdAndAllocationTimeBetween(Long goalId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 按账户ID和时间范围查询分配记录
    List<Allocation> findByAccountIdAndAllocationTimeBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 按可见性查询分配记录
    List<Allocation> findByVisibleScope(String visibleScope);
    
    // 统计目标的总分配金额
    BigDecimal sumAmountByGoalId(Long goalId);
    
    // 统计账户的总分配金额
    BigDecimal sumAmountByAccountId(Long accountId);
}
