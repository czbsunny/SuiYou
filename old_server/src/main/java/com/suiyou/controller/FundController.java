package com.suiyou.controller;

import com.suiyou.dto.es.FundSearchResp;
import com.suiyou.service.FundSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 基金搜索控制器
 * 提供基金搜索相关的API接口
 */
@RestController
@RequestMapping("/fund")
public class FundController {

    private static final Logger logger = LoggerFactory.getLogger(FundController.class);
    
    private final FundSearchService fundSearchService;
    
    @Autowired
    public FundController(FundSearchService fundSearchService) {
        this.fundSearchService = fundSearchService;
    }
    
    /**
     * 搜索基金
     * @param keyword 搜索关键词
     * @param fundType 基金类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<FundSearchResp> searchFunds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fundType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        logger.info("收到基金搜索请求: keyword={}, fundType={}, page={}, pageSize={}", 
                keyword, fundType, page, pageSize);
        
        // 验证参数：至少提供关键词或基金类型之一
        if ((keyword == null || keyword.isEmpty()) && (fundType == null || fundType.isEmpty())) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            FundSearchResp response = fundSearchService.searchFunds(keyword, fundType, page, pageSize);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("基金搜索失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 基金API健康检查
     * @return 健康检查结果
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Fund API is running normally");
        return ResponseEntity.ok(response);
    }
}