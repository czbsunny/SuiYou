package com.suiyou.dto.ocr;
import lombok.Data;

/**
 * 基金信息DTO类
 * 用于存储OCR识别出的基金信息
 */
@Data
public class FundInfoDTO {
    
    // 基金名称
    private String name;
    
    // 基金代码
    private String symbol;
    
    // 持仓金额
    private Double amount;
    
    // 持仓收益
    private Double returnValue;
}