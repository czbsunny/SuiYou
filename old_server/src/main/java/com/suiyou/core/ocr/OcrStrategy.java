package com.suiyou.core.ocr;

import com.suiyou.dto.ocr.OcrResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * OCR识别策略接口
 * 定义不同OCR引擎实现的通用方法
 */
public interface OcrStrategy {
    
    /**
     * 从文件识别文本
     * @param file 图片文件
     * @return OCR识别结果
     */
    OcrResultDTO recognizeFromFile(MultipartFile file) throws IOException;
    
    /**
     * 从输入流识别文本
     * @param inputStream 图片输入流
     * @param filename 文件名
     * @return OCR识别结果
     */
    OcrResultDTO recognizeFromStream(InputStream inputStream, String filename) throws IOException;
    
    /**
     * 从字节数组识别文本
     * @param imageBytes 图片字节数组
     * @param filename 文件名
     * @return OCR识别结果
     */
    OcrResultDTO recognizeFromBytes(byte[] imageBytes, String filename) throws IOException;
    
    /**
     * 获取OCR引擎名称
     * @return 引擎名称
     */
    String getEngineName();
    
    /**
     * 获取OCR引擎配置信息
     * @return 配置信息映射
     */
    Map<String, Object> getConfigurationInfo();
}