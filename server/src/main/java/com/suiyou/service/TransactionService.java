package com.suiyou.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.suiyou.model.Transaction;
import com.suiyou.model.dto.TransactionCreateReqDTO;
import com.suiyou.model.dto.TransactionQueryReqDTO;

public interface TransactionService {
    Transaction createTransaction(Long userId, TransactionCreateReqDTO req);

    void deleteTransaction(Long id);

    Page<Transaction> queryTransactions(Long userId, TransactionQueryReqDTO query, Pageable pageable);
}
