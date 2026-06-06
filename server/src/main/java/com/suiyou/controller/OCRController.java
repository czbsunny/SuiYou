package com.suiyou.controller;

import com.suiyou.dto.ocr.FundImportRespDTO;
import com.suiyou.model.FeatureLimit;
import com.suiyou.service.FundImportService;
import com.suiyou.service.SubscriptionService;
import com.suiyou.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RestController
@RequestMapping("/ocr")
public class OCRController {

    private final FundImportService fundImportService;
    private final SubscriptionService subscriptionService;
    private final Logger logger = LoggerFactory.getLogger(OCRController.class);

    public OCRController(FundImportService fundImportService,
                        SubscriptionService subscriptionService) {
        this.fundImportService = fundImportService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/fund")
    public ResponseEntity<FundImportRespDTO> importFromImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "save_blocks", defaultValue = "true") boolean saveBlocks) {
        Long userId = null;
        try {
            userId = SecurityUtils.getCurrentUserId();
            logger.info("用户 {} 开始导入持仓图像", userId);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new FundImportRespDTO("error", "未登录或登录已过期", null));
            }

            String featureCode = "SCREENSHOT_IMPORT";
            if (!subscriptionService.checkFeatureLimit(userId, featureCode)) {
                FeatureLimit limit = subscriptionService.getFeatureLimit(userId, featureCode);
                String limitInfo = limit != null ? "（" + limit.getDailyLimit() + "次）" : "";
                logger.warn("用户 {} 尝试导入持仓图像，但今日截图导入次数已用完{}", userId, limitInfo);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new FundImportRespDTO("error", "今日截图导入次数已用完" + limitInfo, null));
            }

            FundImportRespDTO response = fundImportService.recognizeFundImage(file, saveBlocks);
            logger.info("用户 {} 成功导入持仓图像，识别到 {} 个基金", userId, response.getFundInfoList() != null ? response.getFundInfoList().size() : 0);
            subscriptionService.recordFeatureUsage(userId, featureCode);
            logger.info("用户 {} 成功记录了 {} 次截图导入功能使用", userId, 1);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("用户 {} 导入持仓图像时发生IO错误: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FundImportRespDTO("error", "持仓图像文件处理失败: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("用户 {} 导入持仓图像时发生未知错误: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FundImportRespDTO("error", "持仓识别过程中发生错误: " + e.getMessage(), null));
        }
    }
}
