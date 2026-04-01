package com.suiyou.service.impl;

import com.suiyou.core.ocr.OcrStrategyFactory;
import com.suiyou.dto.ocr.FundImportRespDTO;
import com.suiyou.dto.ocr.OcrResultDTO;
import com.suiyou.dto.ocr.TextBlockDTO;
import com.suiyou.dto.es.FundSearchBase;
import com.suiyou.dto.ocr.FundInfoDTO;
import com.suiyou.service.FundSearchService;
import com.suiyou.service.FundImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 * 投资组合导入服务实现类
 */
@Service("fundImportService")
public class FundImportServiceImpl implements FundImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(FundImportServiceImpl.class);
    
    // 正则表达式：匹配纯数字（无符号），支持千分位和小数
    private static final Pattern PLAIN_NUMBER_PATTERN = Pattern.compile("(?<!\\S)\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?(?!\\S|[+\\-%])");

    // 正则表达式：匹配带符号数字，支持千分位和小数
    private static final Pattern SIGNED_NUMBER_PATTERN = Pattern.compile("[+-]\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?");
    
    @Autowired
    private OcrStrategyFactory ocrStrategyFactory;
    
    @Autowired
    private FundSearchService fundSearchService;
    
    /**
     * 识别投资组合图像，提取基金信息
     */
    @Override
    public FundImportRespDTO recognizeFundImage(MultipartFile file, boolean saveBlocks) throws IOException {
        logger.info("开始处理投资组合图像导入请求，文件名: {}, 大小: {}KB", 
                file.getOriginalFilename(), file.getSize() / 1024.0);
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("不支持的文件类型，请上传图片文件");
        }
        
        try {
            // 获取当前配置的OCR策略
            var ocrStrategy = ocrStrategyFactory.getCurrentStrategy();
            
            // 执行OCR识别
            OcrResultDTO ocrResult = ocrStrategy.recognizeFromFile(file);
            
            // 根据识别结果按行聚类文本块
            List<List<TextBlockDTO>> lineBlocks = clusterTextBlocksByLine(ocrResult.getTextBlocks());

            // 如果需要保存行块图像
            if (saveBlocks && !lineBlocks.isEmpty()) {
                saveLineBlockImages(file.getBytes(), lineBlocks);
            }

            // 按块提取基金信息
            List<FundInfoDTO> fundInfoList = extractFundInfoFromLineBlocks(lineBlocks);
            
            // 返回基本响应
            FundImportRespDTO response = new FundImportRespDTO(
                    ocrResult.getStatus(), 
                    "OCR识别完成", 
                    fundInfoList);
            return response;
        } catch (Exception e) {
            logger.error("基金图像识别失败", e);
            throw new IOException("OCR识别过程中发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据文本块的y坐标进行聚类，将同一行的文本块分组
     * 类似ocr_handler.py中的行块分割功能，但基于OCR识别结果的文本框进行聚类
     * 
     * @param textBlocks OCR识别的文本块列表
     * @return 按行分组的文本块列表
     */
    private List<List<TextBlockDTO>> clusterTextBlocksByLine(List<TextBlockDTO> textBlocks) {
        if (textBlocks == null || textBlocks.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 复制文本块列表，避免修改原始数据
        List<TextBlockDTO> sortedBlocks = new ArrayList<>(textBlocks);
        
        // 按y坐标排序所有文本块
        sortedBlocks.sort(Comparator.comparingInt(TextBlockDTO::getY));
        
        List<List<TextBlockDTO>> lineBlocks = new ArrayList<>();
        List<TextBlockDTO> currentLine = new ArrayList<>();
        currentLine.add(sortedBlocks.get(0));
        lineBlocks.add(currentLine);
        int lineHeight = sortedBlocks.get(0).getHeight();
        double lineThresholdFactor = 1.8;

        // 遍历所有文本块进行聚类
        for (int i = 1; i < sortedBlocks.size(); i++) {
            TextBlockDTO currentBlock = sortedBlocks.get(i);
            TextBlockDTO lastBlockInLine = currentLine.get(currentLine.size() - 1);
            
            // 计算当前块与最后一个块的垂直距离
            int yDistance = Math.abs(currentBlock.getY() - lastBlockInLine.getY());
            
            // 如果垂直距离小于阈值，认为是同一行
            if (yDistance <= (int)(lineHeight * lineThresholdFactor)) {
                currentLine.add(currentBlock);
                // 计算currentLine中所有元素的平均高度
                int totalHeight = 0;
                for (TextBlockDTO block : currentLine) {
                    totalHeight += block.getHeight();
                }
                lineHeight = totalHeight / currentLine.size();
            } else {
                // 否则开始新的一行
                currentLine = new ArrayList<>();
                currentLine.add(currentBlock);
                lineHeight = currentBlock.getHeight();
                lineBlocks.add(currentLine);
            }
        }
        
        return lineBlocks;
    }
    
    /**
     * 保存行块图像到临时目录
     */
    private void saveLineBlockImages(byte[] imageBytes, List<List<TextBlockDTO>> lineBlocks) throws IOException {
        // 创建保存目录
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // 使用绝对路径确保文件保存在server目录下的tmp文件夹中
        Path serverDir = Paths.get(".").toAbsolutePath().normalize();
        Path saveDir = serverDir.resolve("tmp").resolve("ocr_line_blocks").resolve(timestamp);
        Files.createDirectories(saveDir);
        
        logger.info("开始保存行块图像到: {}", saveDir.toAbsolutePath());
        
        // 读取原始图像
        BufferedImage originalImage = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            originalImage = ImageIO.read(bis);
        }
        
        if (originalImage == null) {
            logger.error("无法读取图像数据，跳过图像保存");
            return;
        }
        
        // 创建原始图像的副本用于绘制标记
        BufferedImage markedImage = new BufferedImage(
                originalImage.getWidth(), 
                originalImage.getHeight(), 
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g2d = markedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 提取所有文本块并按y坐标排序
        List<TextBlockDTO> allBlocks = new ArrayList<>();
        for (List<TextBlockDTO> line : lineBlocks) {
            allBlocks.addAll(line);
        }
        allBlocks.sort(Comparator.comparingInt(TextBlockDTO::getY));
        
        // 为每个文本块绘制边框和序号
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        
        for (int i = 0; i < allBlocks.size(); i++) {
            TextBlockDTO block = allBlocks.get(i);
            // 绘制边框
            g2d.drawRect(block.getX(), block.getY(), block.getWidth(), block.getHeight());
            
            // 绘制序号和y坐标信息
            String label = String.format("%d (y=%d, h=%d)", i + 1, block.getY(), block.getHeight());
            g2d.setColor(Color.WHITE);
            g2d.fillRect(block.getX(), block.getY() - 20, label.length() * 10, 20);
            g2d.setColor(Color.RED);
            g2d.drawString(label, block.getX(), block.getY() - 5);
        }
        
        // 保存标记后的图像
        Path markedImageFile = saveDir.resolve("marked_image_with_blocks.png");
        ImageIO.write(markedImage, "png", markedImageFile.toFile());
        logger.info("已保存标记图像: {}", markedImageFile.toAbsolutePath());
        
        // 释放资源
        g2d.dispose();
        
        for (int i = 0; i < lineBlocks.size(); i++) {
            List<TextBlockDTO> line = lineBlocks.get(i);
            
            // 计算行的边界框
            int minX = line.stream().mapToInt(TextBlockDTO::getX).min().orElse(0);
            int minY = line.stream().mapToInt(TextBlockDTO::getY).min().orElse(0);
            int maxX = line.stream().mapToInt(block -> block.getX() + block.getWidth()).max().orElse(0);
            int maxY = line.stream().mapToInt(block -> block.getY() + block.getHeight()).max().orElse(0);
            
            // 确保坐标不超出原始图像范围
            int imageWidth = originalImage.getWidth();
            int imageHeight = originalImage.getHeight();
            minX = Math.max(0, minX);
            minY = Math.max(0, minY);
            maxX = Math.min(imageWidth, maxX);
            maxY = Math.min(imageHeight, maxY);
            
            // 计算宽度和高度，确保至少为1
            int width = Math.max(1, maxX - minX);
            int height = Math.max(1, maxY - minY);
            
            try {
                // 裁剪图像块
                BufferedImage lineImage = originalImage.getSubimage(minX, minY, width, height);
                
                // 保存裁剪后的图像
                Path imageFile = saveDir.resolve("line_" + (i + 1) + ".png");
                ImageIO.write(lineImage, "png", imageFile.toFile());
                logger.info("已保存行块图像: {}", imageFile.toAbsolutePath());
                
                // 同时保存行块信息到文本文件
                String lineText = line.stream()
                        .sorted(Comparator.comparingInt(TextBlockDTO::getX))
                        .map(TextBlockDTO::getText)
                        .collect(Collectors.joining(" "));
                
                String info = String.format(
                        "Line %d:\n" +
                        "Text: %s\n" +
                        "Bounding box: [%d, %d, %d, %d]\n" +
                        "Blocks count: %d\n",
                        i + 1, lineText, minX, minY, width, height, line.size());
                
                Path infoFile = saveDir.resolve("line_" + (i + 1) + ".txt");
                Files.write(infoFile, info.getBytes());
            } catch (Exception e) {
                logger.error("保存第 {} 个行块图像失败: {}", i + 1, e.getMessage());
            }
        }
        
        // 另外保存完整的OCR结果汇总信息
        Path summaryFile = saveDir.resolve("ocr_summary.txt");
        StringBuilder summary = new StringBuilder();
        summary.append("OCR行块分割结果汇总\n");
        summary.append("生成时间: ").append(new Date()).append("\n");
        summary.append("总行数: " + lineBlocks.size()).append("\n\n");
        
        for (int i = 0; i < lineBlocks.size(); i++) {
            List<TextBlockDTO> line = lineBlocks.get(i);
            line.sort(Comparator.comparingInt(TextBlockDTO::getX));
            String lineText = line.stream().map(TextBlockDTO::getText).collect(Collectors.joining(" "));
            summary.append(String.format("%3d: %s\n", i + 1, lineText));
        }
        
        Files.write(summaryFile, summary.toString().getBytes());
        logger.info("行块图像和信息保存完成，共保存 {} 个行块", lineBlocks.size());
    }

    /**
     * 从行块中提取基金信息
     * 根据不同平台类型调用相应的提取方法
     * 
     * @param lineBlocks 按行分组的文本块列表
     * @return 提取的基金信息列表
     */
    private List<FundInfoDTO> extractFundInfoFromLineBlocks(List<List<TextBlockDTO>> lineBlocks) {
        List<FundInfoDTO> fundInfoList = new ArrayList<>();
        
        if (lineBlocks == null || lineBlocks.isEmpty()) {
            logger.info("行块列表为空，无法提取基金信息");
            return fundInfoList;
        }
        
        // 遍历每行，提取基金相关信息
        for (int i = 0; i < lineBlocks.size(); i++) {
            List<TextBlockDTO> line = lineBlocks.get(i);
            // 按x坐标排序当前行的文本块
            line.sort(Comparator.comparingInt(TextBlockDTO::getX));
            String lineText = line.stream().map(TextBlockDTO::getText).collect(Collectors.joining(" "));

            // 第一步先判断是否有基金信息块
            List<String> lineTexts = line.stream().map(TextBlockDTO::getText).filter(text -> text != null && !text.trim().isEmpty()).collect(Collectors.toList());
            
            // 判断是否包含数字特征
            boolean hasPlainNumber = false;
            boolean hasSignedNumber = false;
            
            // 使用类常量的正则表达式模式
            Pattern plain = PLAIN_NUMBER_PATTERN;
            Pattern signed = SIGNED_NUMBER_PATTERN;

            for (String text : lineTexts) {
                if (!hasPlainNumber) {
                    hasPlainNumber = plain.matcher(text).find();
                }
                if (!hasSignedNumber) {
                    hasSignedNumber = signed.matcher(text).find();
                }
                // 特殊情况处理
                if (!hasSignedNumber && ("0.00".equals(text) || "0.00%".equals(text))) {
                    hasSignedNumber = true;
                }
            }

            if (hasPlainNumber && hasSignedNumber) {
                logger.info("第 {} 行包含潜在的基金信息: {}", i + 1, lineText);
                FundInfoDTO fundInfo = extractFundInfo(line);
                if (Objects.nonNull(fundInfo)) {
                    fundInfoList.add(fundInfo);
                }
            }
        }
        
        // 创建新列表存储有效的基金信息
        List<FundInfoDTO> validFundInfoList = new ArrayList<>();
        
        // 使用传统for循环替代lambda表达式
        for (FundInfoDTO fundInfo : fundInfoList) {
            // 使用FundSearchService接口中定义的1参数方法
            FundSearchBase fundSearchBase = fundSearchService.searchFunds(fundInfo.getName());
            if (fundSearchBase != null) {
                fundInfo.setName(fundSearchBase.getName());
                fundInfo.setSymbol(fundSearchBase.getFundCode());
                logger.info("已更新基金信息: 原始名称={}, 更新后名称={}, 基金代码={}, 得分={}", 
                        fundInfo.getName(), fundSearchBase.getName(), fundSearchBase.getFundCode(), fundSearchBase.getScore());
                // 只添加查询到的有效基金
                validFundInfoList.add(fundInfo);
            } else {
                logger.info("未找到匹配的基金信息: {}", fundInfo.getName());
            }
        }
        
        logger.info("已提取 {} 条有效基金信息", validFundInfoList.size());
        return validFundInfoList;
    }
    
    /**
     * 提取支付宝基金截图中的信息
     * @param lineBlocks 按行分组的文本块列表
     * @return 提取的基金信息
     */
    private FundInfoDTO extractAlipayBlockInfo(List<List<TextBlockDTO>> lineBlocks, List<List<TextBlockDTO>> columnBlocks) {
        if (lineBlocks == null || lineBlocks.size() < 3) {
            return null;
        }
        
        // 只取最后4行
        List<List<TextBlockDTO>> lastFourLines = lineBlocks.subList(Math.max(0, lineBlocks.size() - 4), lineBlocks.size());
        
        // 第1行：基金名称，必须只有一个文本框
        if (lastFourLines.get(0).size() != 1) {
            return null;
        }
        String name = lastFourLines.get(0).get(0).getText();
        
        // 计算ROI宽度用于水平分列
        int roiWidth = getMaxWidth(lineBlocks);
        int nCols = 4;  // 等比例划分
        int colWidth = roiWidth / nCols;
        List<int[]> colRanges = new ArrayList<>();
        for (int i = 0; i < nCols; i++) {
            colRanges.add(new int[]{i * colWidth, (i + 1) * colWidth});
        }
        
        // 第3行：数值行
        if (lastFourLines.size() < 3) {
            return null;
        }
        List<TextBlockDTO> valueLine = lastFourLines.get(2);
        
        Double amount = null;
        Double returnValue = null;
        
        for (TextBlockDTO block : valueLine) {
            int xCenter = block.getX() + block.getWidth() / 2;
            int colIndex = getColumnIndex(xCenter, colRanges);
            String text = block.getText();
            
            if (colIndex == 0) {
                amount = parseDoubleValue(text);
            } else if (colIndex == 2) {
                returnValue = parseDoubleValue(text);
            }
        }
        
        if (amount != null || returnValue != null) {
            FundInfoDTO fundInfo = new FundInfoDTO();
            fundInfo.setName(name);
            fundInfo.setAmount(amount);
            fundInfo.setReturnValue(returnValue);
            return fundInfo;
        }
        
        return null;
    }
    
    /**
     * 提取支付宝简版基金截图中的信息
     * @param lineBlocks 按行分组的文本块列表
     * @return 提取的基金信息
     */
    private FundInfoDTO extractAlipaySimpleBlockInfo(List<List<TextBlockDTO>> lineBlocks, List<List<TextBlockDTO>> columnBlocks) {
        if (lineBlocks == null || lineBlocks.size() < 3) {
            return null;
        }
        
        // 处理4行以上的情况
        if (lineBlocks.size() >= 4) {
            // 检查倒数第4行是否包含"今日收益更新"
            List<TextBlockDTO> fourthLastLine = lineBlocks.get(lineBlocks.size() - 4);
            String fourthLastLineText = String.join("", fourthLastLine.stream()
                    .map(t -> t.getText().replace(" ", ""))
                    .collect(Collectors.toList()));

            // 如果包含"今日收益更新"，则取最后3行，否则返回null
            if (!fourthLastLineText.contains("今日收益更新")) {
                return null;
            }
        }

        List<List<TextBlockDTO>> linesToProcess= lineBlocks.subList(lineBlocks.size() - 3, lineBlocks.size());
        
        // 判断表头行是否包含"金额"、"昨日收益"和"持有收益"
        List<TextBlockDTO> headerLine = linesToProcess.get(1);
        String headerLineText = String.join("", headerLine.stream()
                .map(t -> t.getText().replace(" ", ""))
                .collect(Collectors.toList()));
        
        if (!(headerLineText.contains("金额") && headerLineText.contains("昨日收益") && headerLineText.contains("持有收益"))) {
            return null;
        }
        
        // 名称行按x排序，第一列是基金名称
        List<TextBlockDTO> nameLine = linesToProcess.get(0);
        if (nameLine.isEmpty()) {
            return null;
        }
        // 按x坐标排序
        nameLine.sort(Comparator.comparingInt(TextBlockDTO::getX));
        String name = nameLine.get(0).getText().replace(" ", "");
        
        // 最后一行是数据行
        List<TextBlockDTO> lastLine = linesToProcess.get(2);
        if (lastLine.isEmpty()) {
            return null;
        }
        
        // 按x坐标排序
        lastLine.sort(Comparator.comparingInt(TextBlockDTO::getX));
        
        // 判断最后一行是否至少有3列
        if (lastLine.size() < 3) {
            return null;
        }
        
        // 金额取最后一行第一列
        Double amount = parseDoubleValue(lastLine.get(0).getText().replace(" ", ""));
        // 持有收益取最后一行第三列
        Double returnValue = parseDoubleValue(lastLine.get(2).getText().replace(" ", ""));
        
        if (amount != null || returnValue != null || name != null) {
            FundInfoDTO fundInfo = new FundInfoDTO();
            fundInfo.setName(name);
            fundInfo.setAmount(amount);
            fundInfo.setReturnValue(returnValue);
            return fundInfo;
        }
        
        return null;
    }
    
    /**
     * 提取天天基金截图中的信息
     * @param lineBlocks 按行分组的文本块列表
     * @return 提取的基金信息
     */
    private FundInfoDTO extractTiantianfundBlockInfo(List<List<TextBlockDTO>> lineBlocks, List<List<TextBlockDTO>> columnBlocks) {
        if (lineBlocks == null || lineBlocks.isEmpty()) {
            return null;
        }
        
        // 1) 第一行：名称 + 6位代码
        String name = null;
        String symbol = null;
        List<TextBlockDTO> firstLine = lineBlocks.get(0);
        String firstLineText = String.join(" ", firstLine.stream()
                .map(TextBlockDTO::getText)
                .collect(Collectors.toList()));
        
        // 匹配6位数字代码
        Pattern fundCodePattern = Pattern.compile("\\d{6}");
        Matcher matcher = fundCodePattern.matcher(firstLineText);
        if (matcher.find()) {
            symbol = matcher.group(0);
            name = firstLineText.replace(symbol, "").replace("…", "").replace("...", "").replaceAll("[.，,]", "").trim();
            name = removeInnerSpaces(name);
        } else {
            // 没抓到代码，只记录名称
            name = firstLineText.replace("…", "").replace("...", "").replaceAll("[.，,]", "").trim();
            name = removeInnerSpaces(name);
        }
        
        // 2) 找到关键词所在行（资产/昨日收益/持仓收益/率）
        Integer kwLineIndex = null;
        for (int i = 0; i < Math.min(4, lineBlocks.size()); i++) {  // 通常在前几行
            String joined = String.join(" ", lineBlocks.get(i).stream()
                    .map(TextBlockDTO::getText)
                    .collect(Collectors.toList()));
            if (joined.contains("资产") && joined.contains("昨日收益") && joined.contains("持仓收益")) {
                kwLineIndex = i;
                break;
            }
        }
        
        // 3) 确定列边界
        List<int[]> colRanges;
        int roiWidth = getMaxWidth(lineBlocks);
        if (kwLineIndex != null) {
            // 这里简化处理，实际应该根据关键词行来确定列边界
            int oneThird = roiWidth / 3;
            colRanges = Arrays.asList(
                    new int[]{0, oneThird},
                    new int[]{oneThird, 2 * oneThird},
                    new int[]{2 * oneThird, roiWidth}
            );
        } else {
            // 兜底：等宽三列
            int oneThird = roiWidth / 3;
            colRanges = Arrays.asList(
                    new int[]{0, oneThird},
                    new int[]{oneThird, 2 * oneThird},
                    new int[]{2 * oneThird, roiWidth - 1}
            );
        }
        
        // 4) 提取数值行
        Double amount = null;
        Double returnValue = null;
        
        if (kwLineIndex != null && kwLineIndex + 1 < lineBlocks.size()) {
            List<TextBlockDTO> thirdLine = lineBlocks.get(kwLineIndex + 1);
            Map<Integer, List<String>> colTexts = new HashMap<>();
            for (int i = 0; i < 3; i++) {
                colTexts.put(i, new ArrayList<>());
            }
            
            for (TextBlockDTO block : thirdLine) {
                int xCenter = block.getX() + block.getWidth() / 2;
                int colIndex = getColumnIndex(xCenter, colRanges);
                colTexts.get(colIndex).add(block.getText());
            }
            
            // 资产：优先取"非百分比、无正负"的数值
            for (String text : colTexts.get(0)) {
                amount = parseDoubleValue(text);
                if (amount != null) break;
            }
            
            // 持仓收益：优先取"带正负"的数值
            for (String text : colTexts.get(2)) {
                Matcher signedMatcher = SIGNED_NUMBER_PATTERN.matcher(text);
                if (signedMatcher.find()) {
                    returnValue = parseDoubleValue(signedMatcher.group());
                    break;
                }
            }
        }
        
        if (amount != null || returnValue != null || name != null) {
            FundInfoDTO fundInfo = new FundInfoDTO();
            fundInfo.setName(name);
            fundInfo.setSymbol(symbol);
            fundInfo.setAmount(amount);
            fundInfo.setReturnValue(returnValue);
            return fundInfo;
        }
        
        return null;
    }
    
    /**
     * 提取雪球基金截图中的信息
     * @param lineBlocks 按行分组的文本块列表
     * @return 提取的基金信息
     */
    private FundInfoDTO extractXueqiuBlockInfo(List<List<TextBlockDTO>> lineBlocks, List<List<TextBlockDTO>> columnBlocks) {
        if (lineBlocks == null || lineBlocks.isEmpty()) {
            return null;
        }
        
        if (columnBlocks.size() != 3) {
            return null;
        }
        
        // 提取名称、金额、收益
        List<TextBlockDTO> firstBlock = columnBlocks.get(0);
        if (firstBlock.isEmpty()) {
            return null;
        }
        
        // 第一块包含基金名称和持仓金额
        String name;
        Double amount = null;
        
        if (firstBlock.size() < 2) {
            name = String.join(" ", firstBlock.stream().map(TextBlockDTO::getText).collect(Collectors.toList()));
        } else {
            // 假设最后一行是金额，前面的是基金名称
            List<String> firstBlockTexts = firstBlock.stream().map(TextBlockDTO::getText).collect(Collectors.toList());
            List<String> nameParts = firstBlockTexts.subList(0, firstBlockTexts.size() - 1);
            name = String.join(" ", nameParts);
            
            // 从最后一行提取金额
            Pattern plainPattern = Pattern.compile("\\b(?<![+\\-])\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?\\b");
            Matcher matcher = plainPattern.matcher(firstBlockTexts.get(firstBlockTexts.size() - 1));
            if (matcher.find()) {
                amount = parseDoubleValue(matcher.group());
            }
        }
        
        // 第三块包含持仓收益和收益率
        List<TextBlockDTO> thirdBlock = columnBlocks.get(2);
        Double returnValue = null;
        
        for (TextBlockDTO block : thirdBlock) {
            Matcher matcher = SIGNED_NUMBER_PATTERN.matcher(block.getText());
            if (matcher.find()) {
                returnValue = parseDoubleValue(matcher.group());
                break;
            }
        }
        
        if (amount != null || returnValue != null || name != null) {
            FundInfoDTO fundInfo = new FundInfoDTO();
            fundInfo.setName(name);
            fundInfo.setAmount(amount);
            fundInfo.setReturnValue(returnValue);
            return fundInfo;
        }
        
        return null;
    }
    
    /**
     * 提取支付宝基金主页截图中的信息
     * @param lineBlocks 按行分组的文本块列表
     * @return 提取的基金信息
     */
    private FundInfoDTO extractAlipayMainPageInfo(List<List<TextBlockDTO>> lineBlocks, List<List<TextBlockDTO>> columnBlocks) {
        if (lineBlocks == null || lineBlocks.isEmpty()) {
            return null;
        }
        
        if (columnBlocks.size() != 3) {
            return null;
        }
        
        // 提取名称、金额、收益
        List<TextBlockDTO> firstBlock = columnBlocks.get(0);
        if (firstBlock.isEmpty()) {
            return null;
        }
        
        // 第一块通常包含基金名称
        List<String> firstTwoLines = firstBlock.stream()
                .limit(2)
                .map(TextBlockDTO::getText)
                .collect(Collectors.toList());
        String name = String.join("", firstTwoLines);
        
        // 支付宝基金主页的金额通常在第二块的第一行
        Double amount = null;
        if (columnBlocks.size() > 1 && !columnBlocks.get(1).isEmpty()) {
            TextBlockDTO secondBlockFirstLine = columnBlocks.get(1).get(0);
            Matcher matcher = PLAIN_NUMBER_PATTERN.matcher(secondBlockFirstLine.getText());
            if (matcher.find()) {
                amount = parseDoubleValue(matcher.group());
            }
        }
        
        // 第三块包含持仓收益和收益率
        List<TextBlockDTO> thirdBlock = columnBlocks.get(2);
        Double returnValue = null;
        
        for (TextBlockDTO block : thirdBlock) {
            Matcher matcher = SIGNED_NUMBER_PATTERN.matcher(block.getText());
            if (matcher.find()) {
                returnValue = parseDoubleValue(matcher.group());
                break;
            }
        }
        
        if (amount != null || returnValue != null || name != null) {
            FundInfoDTO fundInfo = new FundInfoDTO();
            fundInfo.setName(name);
            fundInfo.setAmount(amount);
            fundInfo.setReturnValue(returnValue);
            return fundInfo;
        }
        
        return null;
    }
    
    // 辅助方法：获取列索引
    private int getColumnIndex(int xCenter, List<int[]> colRanges) {
        for (int i = 0; i < colRanges.size(); i++) {
            int[] range = colRanges.get(i);
            if (range[0] <= xCenter && xCenter <= range[1]) {
                return i;
            }
        }
        // 找到最近的列
        int minDistance = Integer.MAX_VALUE;
        int closestIndex = 0;
        for (int i = 0; i < colRanges.size(); i++) {
            int[] range = colRanges.get(i);
            int center = (range[0] + range[1]) / 2;
            int distance = Math.abs(xCenter - center);
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
    }
    
    // 辅助方法：解析双精度数值
    private Double parseDoubleValue(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            // 移除千位分隔符
            String cleanText = text.replace(",", "");
            // 尝试解析
            return Double.parseDouble(cleanText);
        } catch (NumberFormatException e) {
            // 如果解析失败，尝试提取数字部分
            Matcher matcher = SIGNED_NUMBER_PATTERN.matcher(text);
            if (matcher.find()) {
                try {
                    String numStr = matcher.group().replace(",", "");
                    return Double.parseDouble(numStr);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return null;
        }
    }
    
    // 辅助方法：获取最大宽度
    private int getMaxWidth(List<List<TextBlockDTO>> lineBlocks) {
        int maxWidth = 0;
        for (List<TextBlockDTO> line : lineBlocks) {
            for (TextBlockDTO block : line) {
                maxWidth = Math.max(maxWidth, block.getX() + block.getWidth());
            }
        }
        return maxWidth;
    }
    
    // 辅助方法：移除内部空格
    private String removeInnerSpaces(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * 根据文字内容判断截图类型
     * @param line 文本块列表
     * @return 截图类型
     */
    private FundInfoDTO extractFundInfo(List<TextBlockDTO> line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        List<List<TextBlockDTO>> groupedLines = groupTextBlocksByLineIntersection(line);
        List<List<TextBlockDTO>> groupedColumns = groupTextBlocksByColumnIntersection(line);
        String lineText = line.stream().map(TextBlockDTO::getText).collect(Collectors.joining(" "));
        // 天天基金特征
        if (lineText.contains("资产") && lineText.contains("持仓收益")) {
            logger.info("提取天天基金持仓信息: {}", lineText);
            return extractTiantianfundBlockInfo(groupedLines, groupedColumns);
        }

        // 支付宝简版特征
        if (lineText.contains("金额") && lineText.contains("昨日收益") && lineText.contains("持有收益")) {
            logger.info("提取支付宝简版持仓信息: {}", lineText);
            return extractAlipaySimpleBlockInfo(groupedLines, groupedColumns);
        }
        
        // 支付宝特征
        if (groupedLines.size() == 4 && lineText.contains("占比") ) {
            logger.info("提取支付宝持仓信息: {}", lineText);
            return extractAlipayBlockInfo(groupedLines, groupedColumns);
        }
        
        // 雪球特征
        if (groupedColumns.size() == 3) {
            if (Pattern.matches(".*\\d{2}-\\d{2}.*", lineText)) {
                logger.info("提取雪球持仓信息: {}", lineText);
                return extractXueqiuBlockInfo(groupedLines, groupedColumns);
            }

            logger.info("提取支付宝主页面持仓信息: {}", lineText);
            return extractAlipayMainPageInfo(groupedLines, groupedColumns);
        }
        
        // 默认为其他类型
        return null;
    }

    /**
     * 根据文本块的y坐标判断相交归属同一行
     * 将文本块列表按行分组，具有相交y区间的文本块归为同一行
     * 
     * @param textBlocks 文本块列表
     * @return 按行分组的文本块列表
     */
    private List<List<TextBlockDTO>> groupTextBlocksByLineIntersection(List<TextBlockDTO> textBlocks) {
        List<List<TextBlockDTO>> lineBlocks = new ArrayList<>();
        
        if (textBlocks == null || textBlocks.isEmpty()) {
            return lineBlocks;
        }
        
        // 复制文本块列表并按y坐标排序
        List<TextBlockDTO> sortedBlocks = new ArrayList<>(textBlocks);
        sortedBlocks.sort(Comparator.comparingInt(TextBlockDTO::getY));
        
        // 为第一个文本块创建新行
        List<TextBlockDTO> currentLine = new ArrayList<>();
        currentLine.add(sortedBlocks.get(0));
        lineBlocks.add(currentLine);
        
        // 遍历剩余的文本块
        for (int i = 1; i < sortedBlocks.size(); i++) {
            TextBlockDTO currentBlock = sortedBlocks.get(i);
            boolean addedToExistingLine = false;
            
            // 检查是否与已有行中的文本块相交
            for (List<TextBlockDTO> line : lineBlocks) {
                for (TextBlockDTO blockInLine : line) {
                    // 检查两个文本块的y区间是否相交
                    if (currentBlock.hasIntersectingYRange(blockInLine)) {
                        line.add(currentBlock);
                        addedToExistingLine = true;
                        break;
                    }
                }
                if (addedToExistingLine) {
                    break;
                }
            }
            
            // 如果没有添加到任何已有行，创建新行
            if (!addedToExistingLine) {
                List<TextBlockDTO> newLine = new ArrayList<>();
                newLine.add(currentBlock);
                lineBlocks.add(newLine);
            }
        }
        
        // 对每行中的文本块按x坐标排序
        for (List<TextBlockDTO> line : lineBlocks) {
            line.sort(Comparator.comparingInt(TextBlockDTO::getX));
        }
        
        return lineBlocks;
    }
    

    
    /**
     * 根据文本块的x坐标判断相交归属同一列
     * 将文本块列表按列分组，具有相交x区间的文本块归为同一列
     * 优化版本：使用列的整体范围来判断，而不仅是与列中的单个文本块比较
     * 
     * @param textBlocks 文本块列表
     * @return 按列分组的文本块列表
     */
    private List<List<TextBlockDTO>> groupTextBlocksByColumnIntersection(List<TextBlockDTO> textBlocks) {
        List<List<TextBlockDTO>> columnBlocks = new ArrayList<>();
        List<int[]> columnRanges = new ArrayList<>(); // 存储每列的x范围 [minX, maxX]
        
        if (textBlocks == null || textBlocks.isEmpty()) {
            return columnBlocks;
        }
        
        // 复制文本块列表并按x坐标排序
        List<TextBlockDTO> sortedBlocks = new ArrayList<>(textBlocks);
        sortedBlocks.sort(Comparator.comparingInt(TextBlockDTO::getX));
        
        // 为第一个文本块创建新列
        List<TextBlockDTO> firstColumn = new ArrayList<>();
        TextBlockDTO firstBlock = sortedBlocks.get(0);
        firstColumn.add(firstBlock);
        columnBlocks.add(firstColumn);
        columnRanges.add(new int[]{firstBlock.getX(), firstBlock.getX() + firstBlock.getWidth()});
        
        // 遍历剩余的文本块
        for (int i = 1; i < sortedBlocks.size(); i++) {
            TextBlockDTO currentBlock = sortedBlocks.get(i);
            boolean addedToExistingColumn = false;
            
            // 计算当前块的x范围
            int blockLeft = currentBlock.getX();
            int blockRight = blockLeft + currentBlock.getWidth();
            
            // 检查是否与已有列的范围相交
            for (int j = 0; j < columnRanges.size(); j++) {
                int[] colRange = columnRanges.get(j);
                
                // 检查当前块的x范围是否与列的x范围相交
                if (blockLeft <= colRange[1] && colRange[0] <= blockRight) {
                    // 相交，添加到该列
                    columnBlocks.get(j).add(currentBlock);
                    
                    // 更新列的范围
                    colRange[0] = Math.min(colRange[0], blockLeft);
                    colRange[1] = Math.max(colRange[1], blockRight);
                    
                    addedToExistingColumn = true;
                    break;
                }
            }
            
            // 如果没有添加到任何已有列，创建新列
            if (!addedToExistingColumn) {
                List<TextBlockDTO> newColumn = new ArrayList<>();
                newColumn.add(currentBlock);
                columnBlocks.add(newColumn);
                columnRanges.add(new int[]{blockLeft, blockRight});
            }
        }
        
        // 对每列中的文本块按y坐标排序
        for (List<TextBlockDTO> column : columnBlocks) {
            column.sort(Comparator.comparingInt(TextBlockDTO::getY));
        }
        
        return columnBlocks;
    }
}