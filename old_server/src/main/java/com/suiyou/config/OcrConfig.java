package com.suiyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * OCR配置类
 */
@Configuration
@ConfigurationProperties(prefix = "ocr")
@Data
public class OcrConfig {

    /**
     * OCR引擎类型: local(EasyOCR), baidu(百度云OCR), alicloud(阿里云OCR)
     */
    private String engine = "local";

    /**
     * 本地OCR配置
     */
    private LocalConfig local = new LocalConfig();

    /**
     * 百度云OCR配置
     */
    private BaiduConfig baidu = new BaiduConfig();

    /**
     * 阿里云OCR配置
     */
    private AliCloudConfig alicloud = new AliCloudConfig();

    /**
     * 通用OCR配置
     */
    private GeneralConfig general = new GeneralConfig();

    /**
     * 本地OCR配置类
     */
    @Data
    public static class LocalConfig {
        private String languages = "ch_sim,en";
        private boolean gpu = false;
        private int port = 7576;
        private String serviceUrl = "http://localhost:%d/api/ocr/recognize-json";
        /**
         * 获取语言列表
         */
        public List<String> getLanguageList() {
            return Arrays.asList(languages.split(","));
        }
    }

    /**
     * 百度云OCR配置类
     */
    @Data
    public static class BaiduConfig {
        private String appId = "";
        private String apiKey = "";
        private String secretKey = "";
        private int timeout = 30;
        private int retryCount = 2;
        private int retryDelay = 1;
    }

    /**
     * 阿里云OCR配置类
     */
    @Data
    public static class AliCloudConfig {
        private String accessKeyId = "";
        private String accessKeySecret = "";
        private String endpoint = "ocr-api.cn-hangzhou.aliyuncs.com";
        private int timeout = 30;
        private int retryCount = 2;
        private int retryDelay = 1;
    }

    /**
     * 通用OCR配置类
     */
    @Data
    public static class GeneralConfig {
        private double confidenceThreshold = 0.5;
        private int maxRetries = 3;
        private int retryInterval = 1;
        private boolean saveRecognitionResults = false;
        private String resultSavePath = "./data/ocr_results";
    }
}