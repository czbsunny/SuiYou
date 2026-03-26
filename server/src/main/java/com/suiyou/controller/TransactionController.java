package com.suiyou.controller;

import com.suiyou.model.Transaction;
import com.suiyou.dto.transaction.TransactionCreateRespDTO;
import com.suiyou.dto.transaction.TransactionQueryRespDTO;
import com.suiyou.dto.transaction.TransactionRespDTO;
import com.suiyou.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Resource
    private TransactionService transactionService;

    /**
     * 新增交易 (记一笔)
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TransactionCreateReqDTO req) {

        try {
            Long userId = getCurrentUserId();
            
            Transaction transaction = transactionService.createTransaction(userId, req);
            
            // 返回创建成功的响应
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "transaction", transaction,
                "message", "交易添加成功"
            ));
        } catch (Exception e) {
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "交易添加失败",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 删除交易 (撤销一笔)
     * 注意：这会触发余额回滚
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 分页查询交易流水
     * 支持筛选: 时间范围、类型、指定资产
     */
    @GetMapping
    public ResponseEntity<Page<TransactionRespDTO>> list(
            TransactionQueryReqDTO query,
            @PageableDefault(sort = "transTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getCurrentUserId();
        Page<TransactionRespDTO> page = transactionService.queryTransactions(userId, query, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * 获取当前登录用户的ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("无法从认证信息中获取用户ID");
        }
    }
}