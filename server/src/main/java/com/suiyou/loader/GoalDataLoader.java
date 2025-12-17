package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.model.SysGoalCategory;
import com.suiyou.model.SysGoalTemplate;
import com.suiyou.repository.SysGoalCategoryRepository;
import com.suiyou.repository.SysGoalTemplateRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@Order(4)
public class GoalDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysGoalCategoryRepository categoryRepository;

    @Autowired
    private SysGoalTemplateRepository templateRepository;

    // 注入之前的两个 JSON 文件
    @Value("classpath:sys_goal_category_init.json")
    private Resource categoryJsonResource;

    @Value("classpath:sys_goal_template_init.json")
    private Resource templateJsonResource;

    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info(">>> 开始初始化目标模块数据...");
        
        // 读取配置文件内容到字节数组，以便多次使用
        byte[] categoryBytes = categoryJsonResource.getInputStream().readAllBytes();
        byte[] templateBytes = templateJsonResource.getInputStream().readAllBytes();
        
        // 计算配置文件的MD5哈希值
        String categoryHash = computeMD5(categoryBytes);
        String templateHash = computeMD5(templateBytes);
        String combinedHash = categoryHash + templateHash;
        String md5Hash = computeMD5(combinedHash.getBytes());
        
        // 1. 同步目标分类
        List<GoalCategoryInitDTO> categoryDtos = objectMapper.readValue(
            categoryBytes,
            new TypeReference<List<GoalCategoryInitDTO>>() {}
        );
        syncGoalCategories(categoryDtos);
        
        // 2. 同步目标模板 (依赖分类的 Code，所以顺序无关，只要分类Code存在即可)
        List<GoalTemplateInitDTO> templateDtos = objectMapper.readValue(
            templateBytes,
            new TypeReference<List<GoalTemplateInitDTO>>() {}
        );
        syncGoalTemplates(templateDtos);
        
        // 更新配置版本
        updateConfigVersion("goal_data", md5Hash);
        
        log.info("<<< 目标模块数据初始化完成。");
    }

    /**
     * 同步目标分类
     */
    private void syncGoalCategories(List<GoalCategoryInitDTO> dtos) throws Exception {
        log.info("正在同步 [目标分类] 数据...");

        List<String> expectedCodes = new ArrayList<>();
        List<SysGoalCategory> entitiesToSave = new ArrayList<>();

        for (GoalCategoryInitDTO dto : dtos) {
            expectedCodes.add(dto.getCategoryCode());
            entitiesToSave.add(processCategory(dto));
        }

        categoryRepository.saveAll(entitiesToSave);

        // 清理数据库中存在，但JSON中已删除的分类
        // 注意：实际生产中删除需谨慎，最好检查是否有用户数据关联
        List<SysGoalCategory> allCategories = categoryRepository.findAll();
        List<String> codesToDelete = allCategories.stream()
                .map(SysGoalCategory::getCategoryCode)
                .filter(code -> !expectedCodes.contains(code))
                .collect(Collectors.toList());

        if (!codesToDelete.isEmpty()) {
            categoryRepository.deleteByCategoryCodeIn(codesToDelete);
            log.info("清理了 {} 个废弃的目标分类: {}", codesToDelete.size(), codesToDelete);
        }

        updateConfigVersion("goal_category", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(dtos)));
    }

    /**
     * 同步目标模板
     */
    private void syncGoalTemplates(List<GoalTemplateInitDTO> dtos) throws Exception {
        log.info("正在同步 [目标模板] 数据...");

        List<String> expectedCodes = new ArrayList<>();
        List<SysGoalTemplate> entitiesToSave = new ArrayList<>();

        for (GoalTemplateInitDTO dto : dtos) {
            expectedCodes.add(dto.getTemplateCode());
            entitiesToSave.add(processTemplate(dto));
        }

        templateRepository.saveAll(entitiesToSave);

        // 清理废弃模板
        List<SysGoalTemplate> allTemplates = templateRepository.findAll();
        List<String> codesToDelete = allTemplates.stream()
                .map(SysGoalTemplate::getTemplateCode)
                .filter(code -> !expectedCodes.contains(code))
                .collect(Collectors.toList());

        if (!codesToDelete.isEmpty()) {
            templateRepository.deleteByTemplateCodeIn(codesToDelete);
            log.info("清理了 {} 个废弃的目标模板: {}", codesToDelete.size(), codesToDelete);
        }
    }

    // --- 辅助处理方法 ---

    private SysGoalCategory processCategory(GoalCategoryInitDTO dto) {
        SysGoalCategory entity = categoryRepository.findByCategoryCode(dto.getCategoryCode());
        if (entity == null) {
            entity = new SysGoalCategory();
            entity.setCategoryCode(dto.getCategoryCode());
        }
        // 覆盖更新属性
        entity.setName(dto.getName());
        entity.setSlogan(dto.getSlogan());
        entity.setIconUrl(dto.getIconUrl());
        entity.setBgColor(dto.getBgColor());
        entity.setSortOrder(dto.getSortOrder());
        entity.setIsEnabled(dto.getIsEnabled());
        return entity;
    }

    private SysGoalTemplate processTemplate(GoalTemplateInitDTO dto) {
        Optional<SysGoalTemplate> optionalEntity = templateRepository.findByTemplateCode(dto.getTemplateCode());
        SysGoalTemplate entity = optionalEntity.orElse(null);
        if (entity == null) {
            entity = new SysGoalTemplate();
            entity.setTemplateCode(dto.getTemplateCode());
        }
        // 覆盖更新属性
        entity.setCategoryCode(dto.getCategoryCode()); // 关联 Code
        entity.setName(dto.getName());
        entity.setIconUrl(dto.getIconUrl());
        entity.setDefaultAmount(dto.getDefaultAmount());
        entity.setDefaultPeriodDays(dto.getDefaultPeriodDays());
        entity.setInputMode(dto.getInputMode());
        entity.setMilestoneStrategy(dto.getMilestoneStrategy());
        entity.setSortOrder(dto.getSortOrder());
        entity.setIsHot(dto.getIsHot());
        
        // JSON 字段处理 (如果JSON文件里有 config 内容)
        // entity.setWizardConfig(dto.getWizardConfig());
        // entity.setMilestoneConfig(dto.getMilestoneConfig());
        
        return entity;
    }

    // --- 内部 DTO 类 (对应 JSON 结构) ---
    // 你也可以将这些提取到 com.suiyou.dto 包下

    @Data
    static class GoalCategoryInitDTO {
        private String categoryCode;
        private String name;
        private String slogan;
        private String iconUrl;
        private String bgColor;
        private Integer sortOrder;
        private Boolean isEnabled;
    }

    @Data
    static class GoalTemplateInitDTO {
        private String categoryCode;
        private String templateCode;
        private String name;
        private String iconUrl;
        private BigDecimal defaultAmount;
        private Integer defaultPeriodDays;
        private String inputMode;
        private String milestoneStrategy;
        private Integer sortOrder;
        private Integer isHot;
        // private String wizardConfig; // 如果json里有字符串格式的json
        // private String milestoneConfig;
    }
    
    // 计算MD5哈希值
    private String computeMD5(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private String computeMD5(String content) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}