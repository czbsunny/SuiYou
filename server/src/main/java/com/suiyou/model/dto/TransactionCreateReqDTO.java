package com.suiyou.model.dto;

import com.suiyou.model.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionCreateReqDTO {
    private TransactionType type; // 枚举: EXPENSE, INCOME, TRANSFER...
    private BigDecimal amount;
    private BigDecimal targetAmount; // 可空，为空时默认等于 amount
    private BigDecimal fee;          // 可空
    private LocalDateTime transTime;
    
    private Long sourceAssetId;
    private Long targetAssetId;
    
    private Long categoryId;
    private String description;
    private List<String> tags; // 接收前端 ["出差", "打车"]
}