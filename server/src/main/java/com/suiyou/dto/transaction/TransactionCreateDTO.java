package com.suiyou.dto.transaction;

import com.suiyou.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionCreateDTO {
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal targetAmount;
    private BigDecimal fee;
    private LocalDateTime transTime;
    
    private Long sourceAssetId;
    private Long targetAssetId;
    
    private Long categoryId;
    private String description;
    private List<String> tags;
    
    private Boolean useFrozenAmount = false;
}