package com.suiyou.repository;

import com.suiyou.model.Transaction;
import com.suiyou.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // 1. 查询某个资产的所有流水 (无论是作为转出方还是转入方)
    // 场景：在资产详情页展示该资产的明细
    @Query("SELECT t FROM Transaction t WHERE t.sourceAssetId = :assetId OR t.targetAssetId = :assetId")
    Page<Transaction> findByAssetId(@Param("assetId") Long assetId, Pageable pageable);

    // 2. 按家庭和时间范围查询 (带分页)
    Page<Transaction> findByFamilyIdAndTransTimeBetween(Long familyId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // 3. 按类型查询
    Page<Transaction> findByFamilyIdAndType(Long familyId, TransactionType type, Pageable pageable);
    
    // 4. (可选) 统计某段时间的总支出
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.familyId = :familyId AND t.type = 'EXPENSE' AND t.transTime BETWEEN :start AND :end")
    BigDecimal sumExpense(@Param("familyId") Long familyId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}