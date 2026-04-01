package com.suiyou.service;

import com.suiyou.dto.ocr.FundImportRespDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 投资组合导入服务接口
 */
public interface FundImportService {
    
    /**
     * 识别投资组合图像，提取基金信息
     *
     * @param file 上传的图像文件
     * @param saveBlocks 是否保存检测到的内容块
     * @return 识别结果DTO
     * @throws IOException 文件处理异常
     */
    FundImportRespDTO recognizeFundImage(MultipartFile file, boolean saveBlocks) throws IOException;
}