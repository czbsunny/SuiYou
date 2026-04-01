package com.suiyou.core.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.config.OcrConfig;
import com.suiyou.dto.ocr.OcrResultDTO;
import com.suiyou.dto.ocr.TextBlockDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.*;

/**
 * 百度云OCR策略实现
 * 集成百度云文字识别服务
 */
@Component("baiduOcrStrategy")
public class BaiduOcrStrategy implements OcrStrategy {

    @Autowired
    private OcrConfig ocrConfig;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String GENERAL_OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general";
    
    private static final Logger logger = LoggerFactory.getLogger(BaiduOcrStrategy.class);
    
    @Override
    public OcrResultDTO recognizeFromFile(MultipartFile file) throws IOException {
        logger.info("使用百度云OCR引擎识别文件: {}", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();
        
        try {
            byte[] imageBytes = file.getBytes();
            OcrResultDTO result = performBaiduRecognition(imageBytes, file.getOriginalFilename());
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            logger.error("百度云OCR识别失败", e);
            OcrResultDTO errorResult = new OcrResultDTO();
            errorResult.setStatus("failed");
            errorResult.setErrorMessage(e.getMessage());
            return errorResult;
        }
    }

    @Override
    public OcrResultDTO recognizeFromStream(InputStream inputStream, String filename) throws IOException {
        logger.info("使用百度云OCR引擎从流识别: {}", filename);
        long startTime = System.currentTimeMillis();
        
        try {
            byte[] imageBytes = inputStream.readAllBytes();
            OcrResultDTO result = performBaiduRecognition(imageBytes, filename);
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            logger.error("百度云OCR流识别失败", e);
            OcrResultDTO errorResult = new OcrResultDTO();
            errorResult.setStatus("failed");
            errorResult.setErrorMessage(e.getMessage());
            return errorResult;
        }
    }

    @Override
    public OcrResultDTO recognizeFromBytes(byte[] imageBytes, String filename) throws IOException {
        logger.info("使用百度云OCR引擎从字节数组识别: {}", filename);
        long startTime = System.currentTimeMillis();
        
        try {
            OcrResultDTO result = performBaiduRecognition(imageBytes, filename);
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            logger.error("百度云OCR字节数组识别失败", e);
            OcrResultDTO errorResult = new OcrResultDTO();
            errorResult.setStatus("failed");
            errorResult.setErrorMessage(e.getMessage());
            return errorResult;
        }
    }

    @Override
    public String getEngineName() {
        return "baidu-cloud-ocr";
    }

    @Override
    public Map<String, Object> getConfigurationInfo() {
        Map<String, Object> configInfo = new HashMap<>();
        configInfo.put("engine", getEngineName());
        configInfo.put("appId", ocrConfig.getBaidu().getAppId());
        configInfo.put("timeout", ocrConfig.getBaidu().getTimeout());
        configInfo.put("retryCount", ocrConfig.getBaidu().getRetryCount());
        return configInfo;
    }

    /**
     * 执行百度云OCR识别
     */
    private OcrResultDTO performBaiduRecognition(byte[] imageBytes, String filename) {
        OcrResultDTO result = new OcrResultDTO();
        // 设置引擎类型
        result.setEngineType(getEngineName());
        try {
            // 检查配置是否完整
            if (ocrConfig.getBaidu().getAppId().isEmpty() || 
                ocrConfig.getBaidu().getApiKey().isEmpty() || 
                ocrConfig.getBaidu().getSecretKey().isEmpty()) {
                throw new IllegalArgumentException("百度云OCR配置不完整");
            }
            
            // 重试逻辑
            int retryCount = ocrConfig.getBaidu().getRetryCount();
            int retryDelay = ocrConfig.getBaidu().getRetryDelay();
            
            for (int retry = 0; retry <= retryCount; retry++) {
                try {
                    // 1. 获取Access Token
                    String accessToken = getBaiduAccessToken();
                    
                    // 2. 调用通用文字识别接口
                    JsonNode ocrResponse = callBaiduOcrApi(imageBytes, accessToken);
                    
                    // 3. 解析识别结果
                    parseOcrResponse(ocrResponse, result);
                    
                    // 设置原始结果
                    Map<String, Object> rawResult = new HashMap<>();
                    rawResult.put("source", "baidu-cloud-ocr");
                    rawResult.put("request_id", ocrResponse.has("log_id") ? ocrResponse.get("log_id").asText() : UUID.randomUUID().toString());
                    rawResult.put("processed_at", new Date());
                    rawResult.put("raw_response", ocrResponse.toString());
                    result.setRawOcrResult(rawResult);
                    
                    return result;
                    
                } catch (Exception e) {
                    // 如果是最后一次重试，则抛出异常
                    if (retry == retryCount) {
                        logger.error("百度云OCR API调用失败，已达到最大重试次数", e);
                        throw e;
                    }
                    logger.warn("百度云OCR API调用失败，准备重试 ({}次/{}次)", retry + 1, retryCount, e);
                    // 等待指定的重试延迟时间
                    Thread.sleep(retryDelay * 1000L);
                }
            }
            
        } catch (Exception e) {
            logger.error("百度云OCR识别处理异常", e);
            result.setStatus("failed");
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取百度云API的访问令牌
     */
    private String getBaiduAccessToken() throws Exception {
        OcrConfig.BaiduConfig config = ocrConfig.getBaidu();
        
        // 构建请求参数
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", config.getApiKey());  // api_key对应client_id
        params.add("client_secret", config.getSecretKey());  // secret_key对应client_secret
        
        try {
            // 发送请求
            String response = restTemplate.postForObject(ACCESS_TOKEN_URL, params, String.class);
            
            // 解析响应
            JsonNode result = objectMapper.readTree(response);
            
            // 检查是否成功获取令牌
            if (result.has("access_token")) {
                return result.get("access_token").asText();
            } else {
                String errorMsg = result.has("error_description") ? 
                    result.get("error_description").asText() : "未知错误";
                logger.error("获取百度云OCR访问令牌失败: {}", result);
                throw new Exception("获取百度云OCR访问令牌失败: " + errorMsg);
            }
            
        } catch (RestClientException e) {
            logger.error("获取百度云OCR访问令牌时发生网络错误", e);
            throw new Exception("获取百度云OCR访问令牌失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用百度云OCR API进行图像识别
     */
    private JsonNode callBaiduOcrApi(byte[] imageBytes, String accessToken) throws Exception {
        // 将图像转换为base64编码并进行urlencode处理
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String urlEncodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8.toString());
        
        // 构建请求体
        String payload = "image=" + urlEncodedImage + 
                        "&detect_direction=false&detect_language=false&vertexes_location=false&paragraph=false&probability=false";
        
        // 构建完整的请求URL
        String fullUrl = GENERAL_OCR_URL + "?access_token=" + accessToken;
        
        try {
            // 发送请求
            String response = restTemplate.postForObject(fullUrl, payload, String.class);
            
            // 解析响应
            return objectMapper.readTree(response);
            
        } catch (RestClientException e) {
            logger.error("百度云OCR API调用失败", e);
            throw new Exception("百度云OCR API调用失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析百度OCR API响应
     */
    private void parseOcrResponse(JsonNode response, OcrResultDTO result) {
        // 检查响应是否包含错误
        if (response.has("error_code")) {
            String errorMsg = response.has("error_msg") ? 
                response.get("error_msg").asText() : "百度OCR API错误";
            result.setStatus("failed");
            result.setErrorMessage(errorMsg);
            return;
        }
        
        // 检查是否包含识别结果
        if (!response.has("words_result") || !response.get("words_result").isArray()) {
            result.setStatus("failed");
            result.setErrorMessage("百度OCR API未返回识别结果");
            return;
        }
        
        // 处理识别结果
        StringBuilder fullTextBuilder = new StringBuilder();
        List<TextBlockDTO> textBlocks = new ArrayList<>();
        
        JsonNode wordsResult = response.get("words_result");
        for (JsonNode item : wordsResult) {
            if (item.has("words")) {
                String text = item.get("words").asText();
                
                // 构建完整文本
                fullTextBuilder.append(text).append("\n");
                
                // 创建文本块
                TextBlockDTO block = new TextBlockDTO();
                block.setText(text);
                
                // 尝试获取位置信息（如果有）
                if (item.has("location")) {
                    JsonNode location = item.get("location");
                    if (location.has("left")) block.setX(location.get("left").asInt());
                    if (location.has("top")) block.setY(location.get("top").asInt());
                    if (location.has("width")) block.setWidth(location.get("width").asInt());
                    if (location.has("height")) block.setHeight(location.get("height").asInt());
                } else {
                    // 默认位置信息
                    block.setX(0);
                    block.setY(0);
                    block.setWidth(0);
                    block.setHeight(0);
                }
                
                // 设置置信度（百度OCR API可能不直接提供，设置默认值）
                block.setConfidence(0.9);
                
                textBlocks.add(block);
            }
        }
        
        // 设置识别结果
        result.setStatus("success");
        result.setFullText(fullTextBuilder.toString().trim());
        result.setTextBlocks(textBlocks);
        
        // 设置文档置信度
        result.setConfidence(0.9);
    }
}