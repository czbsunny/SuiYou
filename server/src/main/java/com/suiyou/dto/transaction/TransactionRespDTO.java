package com.suiyou.dto.transaction;

import com.suiyou.model.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionRespDTO {
    private Long id;
    private TransactionType type;
    private LocalDateTime transTime;
    private BigDecimal amount;
    private BigDecimal targetAmount;
    private BigDecimal fee;
    private Long sourceAssetId;
    private String sourceAccountName;
    private String sourceAccountInstitution;
    private String sourceAccountIdentifier;
    private Long targetAssetId;
    private String targetAccountName;
    private String targetAccountInstitution;
    private String targetAccountIdentifier;
    private Long categoryId;
    private String description;
    private List<String> tags;
    private String location;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}