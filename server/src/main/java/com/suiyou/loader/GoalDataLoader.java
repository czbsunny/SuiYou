package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.model.SysGoalCategory;
import com.suiyou.model.SysGoalTemplate;
import com.suiyou.service.SysGoalConfigService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.suiyou.dto.goal.GoalCategoryInitDTO;
import com.suiyou.dto.goal.GoalTemplateInitDTO;

import java.util.List;

@Component
@Slf4j
@Order(4)
public class GoalDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysGoalConfigService goalConfigService;

    // 注入之前的两个 JSON 文件
    @Value("classpath:sys_goal_category_init.json")
    private Resource categoryJsonResource;

    @Value("classpath:sys_goal_template_init.json")
    private Resource templateJsonResource;

    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info(">>> 开始初始化目标模块数据...");
        
        // 1. 同步目标分类
        List<GoalCategoryInitDTO> categoryDtos = objectMapper.readValue(
            categoryJsonResource.getInputStream().readAllBytes(),
            new TypeReference<List<GoalCategoryInitDTO>>() {}
        );
        goalConfigService.initCategories(categoryDtos);
        
        updateConfigVersion("goal_category", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(categoryDtos)));
        
        // 2. 同步目标模板 (依赖分类的 Code，所以顺序无关，只要分类Code存在即可)
        List<GoalTemplateInitDTO> templateDtos = objectMapper.readValue(
            templateJsonResource.getInputStream().readAllBytes(),
            new TypeReference<List<GoalTemplateInitDTO>>() {}
        );
        goalConfigService.initTemplates(templateDtos);
        
        // 3. 更新配置版本
        updateConfigVersion("goal_template", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(templateDtos)));
        
        log.info("<<< 目标模块数据初始化完成。");
    }
}