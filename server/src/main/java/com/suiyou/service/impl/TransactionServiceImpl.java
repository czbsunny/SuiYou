package com.suiyou.service.impl;

import com.suiyou.model.Transaction;
import com.suiyou.model.Asset;
import com.suiyou.model.Family;
import com.suiyou.model.Account;
import com.suiyou.model.User;
import com.suiyou.model.AccountModule;
import com.suiyou.enums.TransactionType;
import com.suiyou.dto.transaction.TransactionCreateDTO;
import com.suiyou.dto.transaction.TransactionQueryDTO;
import com.suiyou.dto.transaction.TransactionRespDTO;

import com.suiyou.repository.TransactionRepository;
import com.suiyou.repository.AssetRepository;
import com.suiyou.repository.AccountModuleRepository;
import com.suiyou.repository.AccountRepository;
import com.suiyou.repository.UserRepository;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Resource
    private TransactionRepository transactionRepository;

    @Resource
    private AssetRepository assetRepository;

    @Resource
    private AccountModuleRepository accountModuleRepository;

    @Resource
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction createTransaction(Long userId, TransactionCreateDTO req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Transaction trans = new Transaction();
        BeanUtils.copyProperties(req, trans);
        trans.setUserId(userId);
        trans.setFamilyId(user.getFamilyId());
        trans.setUseFrozenAmount(req.getUseFrozenAmount() != null && req.getUseFrozenAmount());
        
        if (trans.getTransTime() == null) trans.setTransTime(LocalDateTime.now());
        if (trans.getTargetAmount() == null) trans.setTargetAmount(trans.getAmount());
        if (trans.getFee() == null) trans.setFee(BigDecimal.ZERO);

        updateAssetBalance(trans, false);

        return transactionRepository.save(trans);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransaction(Long id) {
        Transaction trans = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("交易不存在"));

        updateAssetBalance(trans, true);

        transactionRepository.delete(trans);
    }

    private void updateAssetBalance(Transaction trans, boolean isRollback) {
        BigDecimal amount = trans.getAmount();
        BigDecimal targetAmount = trans.getTargetAmount();
        BigDecimal fee = trans.getFee();

        if (isRollback) {
            amount = amount.negate();
            targetAmount = targetAmount.negate();
            fee = fee.negate();
        }

        switch (trans.getType()) {
            case EXPENSE:
                decreaseAsset(trans.getSourceAssetId(), amount.add(fee), trans.getUseFrozenAmount());
                break;
                
            case INCOME:
                increaseAsset(trans.getTargetAssetId(), targetAmount);
                break;
                
            case TRANSFER:
            case LEND:
            case REPAY:
                decreaseAsset(trans.getSourceAssetId(), amount.add(fee), trans.getUseFrozenAmount());
                increaseAsset(trans.getTargetAssetId(), targetAmount);
                break;
                
            case RECOVER:
            case BORROW:
                decreaseAsset(trans.getSourceAssetId(), amount, trans.getUseFrozenAmount());
                increaseAsset(trans.getTargetAssetId(), targetAmount);
                break;

            case ADJUSTMENT:
            case INVESTMENT_RETURN:
                if (trans.getSourceAssetId() != null) {
                    increaseAsset(trans.getSourceAssetId(), amount);
                }
                break;
        }
    }

    private void increaseAsset(Long assetId, BigDecimal amount) {
        if (assetId == null) return;
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("资产不存在: " + assetId));
        asset.setTotalBalance(asset.getTotalBalance().add(amount));
        assetRepository.save(asset);
    }

    private void decreaseAsset(Long assetId, BigDecimal amount, Boolean useFrozenAmount) {
        if (assetId == null) return;
        
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("资产不存在: " + assetId));

        BigDecimal availableBalance = asset.getTotalBalance().subtract(asset.getFrozenBalance());
        
        if (!Boolean.TRUE.equals(useFrozenAmount)) {
            if (availableBalance.compareTo(amount) < 0) {
                throw new IllegalArgumentException("资产可用余额不足，可用余额: " + availableBalance + ", 需要: " + amount);
            }
        } else {
            if (asset.getTotalBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("资产总余额不足，总余额: " + asset.getTotalBalance() + ", 需要: " + amount);
            }
        }

        asset.setTotalBalance(asset.getTotalBalance().subtract(amount));
        assetRepository.save(asset);
    }

    @Override
    public Page<TransactionRespDTO> queryTransactions(Long userId, TransactionQueryDTO query, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        Specification<Transaction> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("familyId"), user.getFamilyId()));

            if (query.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("transTime"), 
                    query.getStartDate().atStartOfDay()
                ));
            }
            if (query.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("transTime"), 
                    query.getEndDate().atTime(23, 59, 59, 999999999)
                ));
            }

            if (query.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), query.getType()));
            }

            if (query.getAssetId() != null) {
                Predicate isSource = criteriaBuilder.equal(root.get("sourceAssetId"), query.getAssetId());
                Predicate isTarget = criteriaBuilder.equal(root.get("targetAssetId"), query.getAssetId());
                predicates.add(criteriaBuilder.or(isSource, isTarget));
            }

            if (query.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), query.getCategoryId()));
            }

            if (StringUtils.hasText(query.getKeyword())) {
                String likePattern = "%" + query.getKeyword() + "%";
                Predicate descLike = criteriaBuilder.like(root.get("description"), likePattern);
                Predicate locLike = criteriaBuilder.like(root.get("location"), likePattern);
                predicates.add(criteriaBuilder.or(descLike, locLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        return transactions.map(transaction -> {
            TransactionRespDTO dto = new TransactionRespDTO();
            BeanUtils.copyProperties(transaction, dto);
            
            if (transaction.getSourceAssetId() != null) {
                Optional<Asset> sourceAssetOpt = assetRepository.findById(transaction.getSourceAssetId());
                if (sourceAssetOpt.isPresent()) {
                    Asset sourceAsset = sourceAssetOpt.get();
                    if (sourceAsset.getAccountModuleId() != null) {
                        Optional<AccountModule> moduleOpt = accountModuleRepository.findById(sourceAsset.getAccountModuleId());
                        if (moduleOpt.isPresent()) {
                            AccountModule module = moduleOpt.get();
                            Optional<Account> accountOpt = accountRepository.findById(module.getAccountId());
                            if (accountOpt.isPresent()) {
                                Account account = accountOpt.get();
                                dto.setSourceAccountName(account.getAccountName());
                                dto.setSourceAccountInstitution(account.getInstCode());
                                dto.setSourceAccountIdentifier(account.getAccountNo());
                            }
                        }
                    }
                }
            }
            
            if (transaction.getTargetAssetId() != null) {
                Optional<Asset> targetAssetOpt = assetRepository.findById(transaction.getTargetAssetId());
                if (targetAssetOpt.isPresent()) {
                    Asset targetAsset = targetAssetOpt.get();
                    if (targetAsset.getAccountModuleId() != null) {
                        Optional<AccountModule> moduleOpt = accountModuleRepository.findById(targetAsset.getAccountModuleId());
                        if (moduleOpt.isPresent()) {
                            AccountModule module = moduleOpt.get();
                            Optional<Account> accountOpt = accountRepository.findById(module.getAccountId());
                            if (accountOpt.isPresent()) {
                                Account account = accountOpt.get();
                                dto.setTargetAccountName(account.getAccountName());
                                dto.setTargetAccountInstitution(account.getInstCode());
                                dto.setTargetAccountIdentifier(account.getAccountNo());
                            }
                        }
                    }
                }
            }
            
            return dto;
        });
    }

    @Override
    public List<Map<String, Object>> getMonthlyIncomeExpenseTotal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twelveMonthsAgo = now.minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        Specification<Transaction> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("familyId"), user.getFamilyId()));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transTime"), twelveMonthsAgo));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transTime"), now));
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.equal(root.get("type"), TransactionType.INCOME),
                criteriaBuilder.equal(root.get("type"), TransactionType.EXPENSE)
            ));
            predicates.add(criteriaBuilder.equal(root.get("status"), "NORMAL"));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<Transaction> transactions = transactionRepository.findAll(spec);

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDateTime monthDate = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            String monthKey = monthDate.getYear() + "-" + String.format("%02d", monthDate.getMonthValue());
            Map<String, Object> monthData = Map.of(
                "month", monthKey,
                "income", BigDecimal.ZERO,
                "expense", BigDecimal.ZERO
            );
            result.add(0, monthData);
        }

        for (Transaction transaction : transactions) {
            String monthKey = transaction.getTransTime().getYear() + "-" + String.format("%02d", transaction.getTransTime().getMonthValue());
            
            for (int i = 0; i < result.size(); i++) {
                Map<String, Object> monthData = result.get(i);
                if (monthKey.equals(monthData.get("month"))) {
                    BigDecimal income = (BigDecimal) monthData.get("income");
                    BigDecimal expense = (BigDecimal) monthData.get("expense");
                    
                    if (transaction.getType() == TransactionType.INCOME) {
                        income = income.add(transaction.getTargetAmount());
                    } else if (transaction.getType() == TransactionType.EXPENSE) {
                        expense = expense.add(transaction.getAmount());
                    }
                    
                    Map<String, Object> newMonthData = Map.of(
                        "month", monthKey,
                        "income", income,
                        "expense", expense
                    );
                    result.set(i, newMonthData);
                    break;
                }
            }
        }

        return result;
    }
}