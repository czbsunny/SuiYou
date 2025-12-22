package com.suiyou.service.impl;

import com.suiyou.dto.goal.*;
import com.suiyou.service.SysGoalConfigService;

import lombok.extern.slf4j.Slf4j;

import com.suiyou.model.SysGoalCategory;
import com.suiyou.model.SysGoalTemplate;
import com.suiyou.repository.SysGoalCategoryRepository;
import com.suiyou.repository.SysGoalTemplateRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@Service
public class SysGoalConfigServiceImpl implements SysGoalConfigService {
    @Autowired
    private SysGoalCategoryRepository categoryRepository;

    @Autowired
    private SysGoalTemplateRepository templateRepository;
    
    /**
     * 获取所有目标分类
     * @return 目标分类列表
     */
    @Override
    public List<GoalCategoryRespDTO> getAllCategories() {
        return null;
    }
    
    /**
     * 获取所有目标模板
     * @return 目标模板列表
     */
    @Override
    public List<GoalTemplateRespDTO> getAllTemplates() {
        return null;
    }

    @Override
    public void initCategories(List<GoalCategoryInitDTO> dtos) {
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
    }

    @Override
    public void initTemplates(List<GoalTemplateInitDTO> dtos) {
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
        
        return entity;
    } 
}
