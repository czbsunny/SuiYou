package com.suiyou.dto.ocr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 投资组合导入响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundImportRespDTO {
    
    /**
     * 状态：success 或 error
     */
    private String status;
    
    /**
     * 消息描述
     */
    private String message;
    
    /**
     * 识别到的基金信息列表
     */
    private List<FundInfoDTO> fundInfoList;
}