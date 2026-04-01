package com.suiyou.core.ocr;

import com.suiyou.config.OcrConfig;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * OCR策略工厂类
 * 根据配置动态创建和管理不同的OCR策略实例
 */
@Component
public class OcrStrategyFactory {

    @Autowired
    private OcrConfig ocrConfig;

    @Autowired
    @Qualifier("localOcrStrategy")
    private OcrStrategy localOcrStrategy;

    @Autowired
    @Qualifier("baiduOcrStrategy")
    private OcrStrategy baiduOcrStrategy;

    @Autowired
    @Qualifier("aliCloudOcrStrategy")
    private OcrStrategy aliCloudOcrStrategy;

    // 策略映射缓存
    private final Map<String, OcrStrategy> strategyMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(OcrStrategyFactory.class);
    
    /**
     * 初始化策略映射
     */
    @PostConstruct
    public void init() {
        strategyMap.put("local", localOcrStrategy);
        strategyMap.put("baidu", baiduOcrStrategy);
        strategyMap.put("alicloud", aliCloudOcrStrategy);
        logger.info("OCR策略工厂初始化完成，支持的引擎: {}", strategyMap.keySet());
    }

    /**
     * 获取当前配置的OCR策略
     * @return OCR策略实例
     */
    public OcrStrategy getCurrentStrategy() {
        String engineType = ocrConfig.getEngine().toLowerCase();
        OcrStrategy strategy = strategyMap.get(engineType);
        
        if (strategy == null) {
            logger.warn("未找到指定的OCR引擎: {}, 使用默认引擎: local", engineType);
            strategy = localOcrStrategy;
        }
        
        logger.info("使用OCR引擎: {}", strategy.getEngineName());
        return strategy;
    }

    /**
     * 根据指定的引擎类型获取OCR策略
     * @param engineType 引擎类型
     * @return OCR策略实例
     */
    public OcrStrategy getStrategy(String engineType) {
        if (engineType == null || engineType.trim().isEmpty()) {
            return getCurrentStrategy();
        }
        
        String type = engineType.toLowerCase();
        OcrStrategy strategy = strategyMap.get(type);
        
        if (strategy == null) {
            logger.warn("未找到指定的OCR引擎: {}, 使用默认引擎: local", type);
            strategy = localOcrStrategy;
        }
        
        logger.info("使用指定的OCR引擎: {}", strategy.getEngineName());
        return strategy;
    }

    /**
     * 获取所有支持的OCR策略
     * @return 策略映射
     */
    public Map<String, OcrStrategy> getAllStrategies() {
        return new HashMap<>(strategyMap);
    }

    /**
     * 获取当前引擎的配置信息
     * @return 配置信息映射
     */
    public Map<String, Object> getCurrentEngineConfig() {
        OcrStrategy strategy = getCurrentStrategy();
        return strategy.getConfigurationInfo();
    }
}