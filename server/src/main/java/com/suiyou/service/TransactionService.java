package com.suiyou.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.suiyou.model.Transaction;
import com.suiyou.dto.transaction.TransactionCreateDTO;
import com.suiyou.dto.transaction.TransactionQueryDTO;
import com.suiyou.dto.transaction.TransactionRespDTO;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    Transaction createTransaction(Long userId, TransactionCreateDTO req);

    void deleteTransaction(Long id);

    Page<TransactionRespDTO> queryTransactions(Long userId, TransactionQueryDTO query, Pageable pageable);

    List<Map<String, Object>> getMonthlyIncomeExpenseTotal(Long userId);
}