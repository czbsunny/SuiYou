package com.suiyou.core.ocr;

import com.suiyou.config.OcrConfig;
import com.suiyou.dto.ocr.OcrResultDTO;
import com.suiyou.dto.ocr.TextBlockDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Base64;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 本地OCR策略实现
 * 调用本地运行的Python OCR服务进行文本识别
 */
@Component("localOcrStrategy")
public class LocalOcrStrategy implements OcrStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LocalOcrStrategy.class);

    @Autowired
    private OcrConfig ocrConfig;
    
    private String pythonOcrServiceUrl;
    
    /**
     * 初始化HTTP客户端
     */
    @PostConstruct
    public void init() {
        try {
            // 可以从配置中读取服务地址
            OcrConfig.LocalConfig localConfig = ocrConfig.getLocal();

            this.pythonOcrServiceUrl = String.format(localConfig.getServiceUrl(), localConfig.getPort());

            logger.info("本地OCR服务客户端初始化完成，服务地址: {}", pythonOcrServiceUrl);
        } catch (Exception e) {
            logger.error("OCR客户端初始化失败", e);
            throw new RuntimeException("OCR客户端初始化失败", e);
        }
    }
    
    @Override
    public OcrResultDTO recognizeFromFile(MultipartFile file) throws IOException {
        logger.info("使用本地Python OCR服务识别文件: {}", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();
        
        try {
            byte[] imageBytes = file.getBytes();
            OcrResultDTO result = callPythonOcrService(imageBytes, file.getOriginalFilename());
            
            // 设置处理时间
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;
            
        } catch (Exception e) {
            logger.error("调用Python OCR服务失败", e);
            OcrResultDTO errorResult = new OcrResultDTO();
            errorResult.setStatus("failed");
            errorResult.setErrorMessage("调用Python OCR服务失败: " + e.getMessage());
            errorResult.setProcessingTime(System.currentTimeMillis() - startTime);
            return errorResult;
        }
    }

    @Override
    public OcrResultDTO recognizeFromStream(InputStream inputStream, String filename) throws IOException {
        logger.info("使用本地Python OCR服务从流识别: {}", filename);
        long startTime = System.currentTimeMillis();
        
        try {
            // 读取输入流到字节数组
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[8192];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] imageBytes = buffer.toByteArray();
            
            OcrResultDTO result = callPythonOcrService(imageBytes, filename);
            
            // 设置处理时间
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;
            
        } catch (Exception e) {
            logger.error("调用Python OCR服务失败", e);
            OcrResultDTO errorResult = new OcrResultDTO();
            errorResult.setStatus("failed");
            errorResult.setErrorMessage("调用Python OCR服务失败: " + e.getMessage());
            errorResult.setProcessingTime(System.currentTimeMillis() - startTime);
            return errorResult;
        }
    }

    @Override
    public OcrResultDTO recognizeFromBytes(byte[] imageBytes, String filename) throws IOException {
        logger.info("使用本地Python OCR服务从字节数组识别: {}", filename);
        long startTime = System.currentTimeMillis();
        
        try {
            OcrResultDTO result = callPythonOcrService(imageBytes, filename);
            
            // 设置处理时间
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;
            
        } catch (Exception e) {
            logger.error("调用Python OCR服务失败", e);
            OcrResultDTO errorResult = new OcrResultDTO();
            errorResult.setStatus("failed");
            errorResult.setErrorMessage("调用Python OCR服务失败: " + e.getMessage());
            errorResult.setProcessingTime(System.currentTimeMillis() - startTime);
            return errorResult;
        }
    }

    @Override
    public String getEngineName() {
        return "local-python-ocr";
    }

    @Override
    public Map<String, Object> getConfigurationInfo() {
        Map<String, Object> configInfo = new HashMap<>();
        configInfo.put("engine", getEngineName());
        configInfo.put("service_url", pythonOcrServiceUrl);
        configInfo.put("languages", ocrConfig.getLocal().getLanguages());
        return configInfo;
    }

    /**
     * 调用本地Python OCR服务
     * @param imageBytes 图片字节数组
     * @param filename 文件名
     * @return OCR识别结果
     */
    private OcrResultDTO callPythonOcrService(byte[] imageBytes, String filename) throws Exception {
        OcrResultDTO result = new OcrResultDTO();
        result.setEngineType(getEngineName());
        
        try {
            // 创建URL对象
            URL url = new URL(pythonOcrServiceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和头信息
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // 构建JSON请求体
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            // 转义文件名中的特殊字符
            String escapedFilename = filename.replace("\"", "\\\"");
            String jsonRequestBody = "{\"filename\": \"" + escapedFilename + "\", \"image\": \"" + base64Image + "\"}";
            
            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequestBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                // 简单解析JSON响应
                return parseJsonResponse(response.toString(), result);
            } else {
                logger.error("Python OCR服务返回非成功状态码: {}", responseCode);
                result.setStatus("failed");
                result.setErrorMessage("Python OCR服务返回错误状态码: " + responseCode);
            }
        } catch (Exception e) {
            logger.error("调用Python OCR服务异常", e);
            throw new RuntimeException("调用Python OCR服务异常", e);
        }
        
        return result;
    }
    
    /**
     * 解析JSON响应
     * @param jsonResponse JSON响应字符串
     * @param result OCR结果对象
     * @return 填充后的OCR结果对象
     */
    private OcrResultDTO parseJsonResponse(String jsonResponse, OcrResultDTO result) {
        try {
            // 使用Jackson的ObjectMapper解析JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // 提取状态
            if (rootNode.has("status")) {
                result.setStatus(rootNode.get("status").asText("success"));
            } else {
                result.setStatus("success"); // 默认成功
            }
            
            // 提取完整文本
            if (rootNode.has("full_text")) {
                result.setFullText(rootNode.get("full_text").asText(""));
            }
            
            // 创建文本块列表
            List<TextBlockDTO> textBlocks = new ArrayList<>();
            
            // 解析text_blocks数组
            if (rootNode.has("text_blocks") && rootNode.get("text_blocks").isArray()) {
                JsonNode textBlocksNode = rootNode.get("text_blocks");
                
                for (JsonNode blockNode : textBlocksNode) {
                    TextBlockDTO block = new TextBlockDTO();
                    
                    // 设置文本内容
                    if (blockNode.has("text")) {
                        block.setText(blockNode.get("text").asText(""));
                    }
                    
                    // 设置置信度
                    if (blockNode.has("confidence")) {
                        block.setConfidence(blockNode.get("confidence").asDouble(0.0));
                    }
                    
                    // 处理边界框信息
                    if (blockNode.has("bbox") && blockNode.get("bbox").isArray()) {
                        JsonNode bboxNode = blockNode.get("bbox");
                        if (bboxNode.size() >= 4) {
                            // 假设bbox格式为[[x1,y1],[x2,y2],[x3,y3],[x4,y4]]
                            // 计算最小和最大坐标值，以确定实际边界
                            int minX = Integer.MAX_VALUE;
                            int minY = Integer.MAX_VALUE;
                            int maxX = Integer.MIN_VALUE;
                            int maxY = Integer.MIN_VALUE;
                            
                            // 遍历所有四个点，找出最小和最大坐标
                            for (int i = 0; i < 4; i++) {
                                JsonNode point = bboxNode.get(i);
                                if (point.isArray() && point.size() >= 2) {
                                    int x = point.get(0).asInt();
                                    int y = point.get(1).asInt();
                                    
                                    minX = Math.min(minX, x);
                                    minY = Math.min(minY, y);
                                    maxX = Math.max(maxX, x);
                                    maxY = Math.max(maxY, y);
                                }
                            }
                            
                            // 只有当我们找到了有效的坐标范围时才设置值
                            if (minX < maxX && minY < maxY) {
                                block.setX(minX);
                                block.setY(minY);
                                block.setWidth(maxX - minX); // 计算实际宽度
                                block.setHeight(maxY - minY); // 计算实际高度
                            } else if (minX != Integer.MAX_VALUE) {
                                // 如果只有一个点有效，使用默认的尺寸
                                block.setX(minX);
                                block.setY(minY);
                                block.setWidth(100);
                                block.setHeight(20);
                            }
                        }
                    }
                    
                    // 如果没有边界框信息，设置默认值
                    if (block.getX() == 0 && block.getY() == 0 && block.getWidth() == 0 && block.getHeight() == 0) {
                        block.setX(0);
                        block.setY(0);
                        block.setWidth(100);
                        block.setHeight(20);
                    }
                    
                    textBlocks.add(block);
                }
            }
            
            // 如果没有解析到文本块但有完整文本，创建一个基本文本块
            if (textBlocks.isEmpty() && result.getFullText() != null && !result.getFullText().isEmpty()) {
                TextBlockDTO block = new TextBlockDTO();
                block.setText(result.getFullText());
                block.setX(0);
                block.setY(0);
                block.setWidth(100);
                block.setHeight(20);
                block.setConfidence(0.95); // 默认置信度
                textBlocks.add(block);
            }
            
            // 设置文本块列表
            result.setTextBlocks(textBlocks);
            
            // 计算平均置信度作为整体置信度
            if (!textBlocks.isEmpty()) {
                double avgConfidence = textBlocks.stream()
                        .mapToDouble(TextBlockDTO::getConfidence)
                        .average()
                        .orElse(0.95);
                result.setConfidence(avgConfidence);
            } else {
                result.setConfidence(0.95); // 默认置信度
            }
            
            // 设置原始结果
            Map<String, Object> rawResult = new HashMap<>();
            rawResult.put("source", getEngineName());
            rawResult.put("processed_at", new Date());
            rawResult.put("python_response", jsonResponse);
            
            // 提取total_blocks
            if (rootNode.has("total_blocks")) {
                rawResult.put("total_blocks", rootNode.get("total_blocks").asInt());
            }
            
            result.setRawOcrResult(rawResult);
            
        } catch (Exception e) {
            logger.error("解析Python OCR响应失败", e);
            result.setStatus("failed");
            result.setErrorMessage("解析OCR响应失败: " + e.getMessage());
        }
        
        return result;
    }
}