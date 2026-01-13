package com.suiyou.model.dto;

import com.suiyou.model.enums.TransactionType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TransactionQueryReqDTO {

    /**
     * 开始日期 (前端传 yyyy-MM-dd)
     * e.g. 2025-01-01
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期 (前端传 yyyy-MM-dd)
     * e.g. 2025-01-31
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 交易类型
     * (EXPENSE, INCOME, TRANSFER...)
     */
    private TransactionType type;

    /**
     * 资产ID
     * 用于查询某个特定账户/资产的流水
     * 逻辑：查询 sourceAssetId = id OR targetAssetId = id
     */
    private Long assetId;

    /**
     * 分类ID
     * 用于筛选 "餐饮"、"交通" 等特定类别的消费
     */
    private Long categoryId;

    /**
     * 关键词搜索
     * 用于模糊匹配 description (备注) 或 location (地点)
     */
    private String keyword;
}