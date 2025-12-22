package com.suiyou.loader;

import com.suiyou.model.SysCategoryInstitutionRelation;
import com.suiyou.model.SysInstitution;
import com.suiyou.dto.account.RelationRuleConfigDTO;

import com.suiyou.repository.SysCategoryInstitutionRelationRepository;
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.service.SysAssetConfigService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Order(3)
@Slf4j
public class RelationDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysAssetConfigService sysAssetCategoryService;

    @Value("classpath:sys_relation_rules.json")
    private Resource jsonResource;

    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始根据配置规则生成机构关联关系...");

        if (!jsonResource.exists()) {
            log.warn("关联规则配置文件不存在，跳过加载");
            return;
        }

        // 1. 读取配置
        RelationRuleConfigDTO config = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(), 
            RelationRuleConfigDTO.class
        );

        // 2. 初始化关联关系
        sysAssetCategoryService.initCategoryInstitutionRelations(config);

        // 3. 更新配置版本
        updateConfigVersion("relation", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(config)));
    }
}