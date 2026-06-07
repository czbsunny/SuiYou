package com.suiyou.controller;

import com.suiyou.model.Transaction;
import com.suiyou.dto.transaction.TransactionCreateRespDTO;
import com.suiyou.dto.transaction.TransactionQueryRespDTO;
import com.suiyou.dto.transaction.TransactionRespDTO;
import com.suiyou.service.TransactionService;
import com.suiyou.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Resource
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TransactionCreateRespDTO req) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Transaction transaction = transactionService.createTransaction(userId, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "transaction", transaction,
                "message", "交易添加成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "交易添加失败",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<TransactionRespDTO>> list(
            TransactionQueryRespDTO query,
            @PageableDefault(sort = "transTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<TransactionRespDTO> page = transactionService.queryTransactions(userId, query, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<List<Map<String, Object>>> getMonthlySummary() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> monthlySummary = transactionService.getMonthlyIncomeExpenseTotal(userId);
        return ResponseEntity.ok(monthlySummary);
    }
}
