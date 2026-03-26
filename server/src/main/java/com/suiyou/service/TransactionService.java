package com.suiyou.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.suiyou.model.Transaction;
import com.suiyou.dto.transaction.TransactionCreateRespDTO;
import com.suiyou.dto.transaction.TransactionQueryRespDTO;
import com.suiyou.dto.transaction.TransactionRespDTO;

public interface TransactionService {
    Transaction createTransaction(Long userId, TransactionCreateRespDTO req);

    void deleteTransaction(Long id);

    Page<TransactionRespDTO> queryTransactions(Long userId, TransactionQueryRespDTO query, Pageable pageable);
}
