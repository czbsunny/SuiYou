package com.suiyou.service.impl;

import com.suiyou.model.Transaction;
import com.suiyou.model.Asset;
import com.suiyou.model.Family;

import com.suiyou.model.dto.TransactionCreateReqDTO;
import com.suiyou.model.dto.TransactionQueryReqDTO;

import com.suiyou.repository.TransactionRepository;
import com.suiyou.repository.AssetRepository;
import com.suiyou.service.FamilyService;
import com.suiyou.service.TransactionService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Resource
    private TransactionRepository transactionRepository;

    @Resource
    private AssetRepository assetRepository;

    @Autowired
    private FamilyService familyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction createTransaction(Long userId, TransactionCreateReqDTO req) {
        Family family = familyService.getFirstActiveFamilyByUserId(userId);

        // 1. 构建交易实体
        Transaction trans = new Transaction();
        BeanUtils.copyProperties(req, trans);
        trans.setUserId(userId);
        trans.setFamilyId(family.getId());
        
        // 处理默认值
        if (trans.getTransTime() == null) trans.setTransTime(LocalDateTime.now());
        if (trans.getTargetAmount() == null) trans.setTargetAmount(trans.getAmount());
        if (trans.getFee() == null) trans.setFee(BigDecimal.ZERO);

        // JSON 字段处理 (如果是 JPA 需要转换 List string 到 JSON string，或者用 Converter)
        // trans.setTags(JSON.toJSONString(req.getTags())); 

        // 2. 核心逻辑：更新资产余额 (根据不同类型)
        updateAssetBalance(trans, false); // false 表示正向操作

        // 3. 保存交易记录
        return transactionRepository.save(trans);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransaction(Long id) {
        Transaction trans = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("交易不存在"));

        // 1. 回滚资产余额 (这是一个逆向操作)
        updateAssetBalance(trans, true); // true 表示回滚/反向操作

        // 2. 删除记录 (或者逻辑删除 setStatus("DELETED"))
        transactionRepository.delete(trans);
    }

    /**
     * 更新资产余额的核心方法
     * @param trans 交易实体
     * @param isRollback 是否是回滚操作 (删除交易时用)
     */
    private void updateAssetBalance(Transaction trans, boolean isRollback) {
        BigDecimal amount = trans.getAmount();
        BigDecimal targetAmount = trans.getTargetAmount();
        BigDecimal fee = trans.getFee();

        // 如果是回滚，数值取反
        if (isRollback) {
            amount = amount.negate();
            targetAmount = targetAmount.negate();
            fee = fee.negate();
        }

        switch (trans.getType()) {
            case EXPENSE:
                // 支出：源资产减少
                decreaseAsset(trans.getSourceAssetId(), amount.add(fee));
                break;
                
            case INCOME:
                // 收入：目标资产增加
                increaseAsset(trans.getTargetAssetId(), targetAmount);
                break;
                
            case TRANSFER:
            case LEND:    // 借出 (钱少了，债权多了)
            case REPAY:   // 还债 (钱少了，负债少了/债权少了)
                // 转账类：源减少，目标增加
                // 注意：手续费通常扣在源资产
                decreaseAsset(trans.getSourceAssetId(), amount.add(fee));
                increaseAsset(trans.getTargetAssetId(), targetAmount);
                break;
                
            case RECOVER: // 收债/报销 (债权少了，钱多了)
            case BORROW:  // 借入 (负债多了，钱多了)
                decreaseAsset(trans.getSourceAssetId(), amount); // 债权/负债来源减少
                increaseAsset(trans.getTargetAssetId(), targetAmount); // 钱包增加
                break;

            case ADJUSTMENT:
                // 余额校准：直接修改余额，通常比较复杂，这里简化为视作收入或支出
                // 实际建议：ADJUSTMENT 单独处理，直接 setBalance，这里略
                if (trans.getTargetAssetId() != null) {
                    increaseAsset(trans.getTargetAssetId(), targetAmount); // 正数加，负数减
                }
                break;
        }
    }

    // 辅助方法：增加余额
    private void increaseAsset(Long assetId, BigDecimal amount) {
        if (assetId == null) return;
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("资产不存在: " + assetId));
        asset.setTotalBalance(asset.getTotalBalance().add(amount));
        assetRepository.save(asset);
    }

    // 辅助方法：减少余额
    private void decreaseAsset(Long assetId, BigDecimal amount) {
        if (assetId == null) return;
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("资产不存在: " + assetId));
        asset.setTotalBalance(asset.getTotalBalance().subtract(amount));
        assetRepository.save(asset);
    }

    @Override
    public Page<Transaction> queryTransactions(Long userId, TransactionQueryReqDTO query, Pageable pageable) {
        Family family = familyService.getFirstActiveFamilyByUserId(userId);
        if (Objects.isNull(family)) {
            throw new IllegalArgumentException("用户未关联任何家庭");
        }
        Specification<Transaction> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 强制家庭隔离
            predicates.add(criteriaBuilder.equal(root.get("familyId"), family.getId()));

            // 2. 时间范围筛选 (LocalDate -> LocalDateTime)
            if (query.getStartDate() != null) {
                // >= 2025-01-01 00:00:00
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("transTime"), 
                    query.getStartDate().atStartOfDay()
                ));
            }
            if (query.getEndDate() != null) {
                // <= 2025-01-01 23:59:59.999999
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("transTime"), 
                    query.getEndDate().atTime(23, 59, 59, 999999999)
                ));
            }

            // 3. 类型筛选
            if (query.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), query.getType()));
            }

            // 4. 资产筛选 (作为源 OR 作为目标)
            if (query.getAssetId() != null) {
                Predicate isSource = criteriaBuilder.equal(root.get("sourceAssetId"), query.getAssetId());
                Predicate isTarget = criteriaBuilder.equal(root.get("targetAssetId"), query.getAssetId());
                predicates.add(criteriaBuilder.or(isSource, isTarget));
            }

            // 5. 分类筛选
            if (query.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), query.getCategoryId()));
            }

            // 6. 关键词模糊搜索 (备注 OR 地点)
            if (StringUtils.hasText(query.getKeyword())) {
                String likePattern = "%" + query.getKeyword() + "%";
                Predicate descLike = criteriaBuilder.like(root.get("description"), likePattern);
                Predicate locLike = criteriaBuilder.like(root.get("location"), likePattern);
                predicates.add(criteriaBuilder.or(descLike, locLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return transactionRepository.findAll(spec, pageable);
    }
}