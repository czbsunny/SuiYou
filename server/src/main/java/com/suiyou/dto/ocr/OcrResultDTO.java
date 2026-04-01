package com.suiyou.dto.ocr;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * OCR识别结果DTO类
 * 用于API响应中传输OCR识别结果信息
 */
@Data
public class OcrResultDTO {
    
    // 识别状态："success"(成功), "failed"(失败)
    private String status;
    
    // 识别的图片类型
    private String imageType;
    
    // 识别到的文本内容（完整）
    private String fullText;
    
    // 识别到的文本块列表
    private List<TextBlockDTO> textBlocks;
    
    // OCR引擎类型
    private String engineType;
    
    // 错误信息（如果识别失败）
    private String errorMessage;
    
    // 识别置信度
    private double confidence;
    
    // 识别耗时（毫秒）
    private long processingTime;
    
    // 原始OCR引擎返回的结果（用于调试）
    private Map<String, Object> rawOcrResult;
}