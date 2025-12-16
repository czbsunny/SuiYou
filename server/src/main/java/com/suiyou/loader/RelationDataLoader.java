package com.suiyou.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.dto.account.RelationRuleConfigDTO;
import com.suiyou.model.SysCategoryInstitutionRelation;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.SysCategoryInstitutionRelationRepository;
import com.suiyou.repository.SysInstitutionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Order(3) // 确保最后执行
@Slf4j
public class RelationDataLoader implements CommandLineRunner {

    @Autowired
    private SysInstitutionRepository institutionRepository;

    @Autowired
    private SysCategoryInstitutionRelationRepository relationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:sys_relation_rules.json")
    private Resource jsonResource;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始根据配置规则生成机构关联关系...");

        if (!jsonResource.exists()) {
            log.warn("关联规则配置文件不存在，跳过加载");
            return;
        }

        // 1. 读取配置
        RelationRuleConfigDTO config = objectMapper.readValue(
            jsonResource.getInputStream(), 
            RelationRuleConfigDTO.class
        );

        // 将特殊规则 List 转为 Map 方便快速查找 (Key: instCode, Value: Categories)
        Map<String, List<String>> specialRuleMap = new HashMap<>();
        if (config.getSpecialCases() != null) {
            specialRuleMap = config.getSpecialCases().stream()
                .collect(Collectors.toMap(
                    RelationRuleConfigDTO.SpecialCaseDTO::getInstCode,
                    RelationRuleConfigDTO.SpecialCaseDTO::getCategories,
                    (v1, v2) -> v1 // 如果有重复key，取第一个
                ));
        }

        // 2. 获取数据库中所有机构
        List<SysInstitution> allInsts = institutionRepository.findAll();

        // 3. 获取数据库中已有的关联关系，构建查重Set (格式: "INST_CODE#CAT_CODE")
        // 这样可以避免在循环中频繁查询数据库，极大提升性能
        Set<String> existingRelations = relationRepository.findAll().stream()
                .map(r -> r.getInstCode() + "#" + r.getCategoryCode())
                .collect(Collectors.toSet());

        List<SysCategoryInstitutionRelation> entitiesToSave = new ArrayList<>();

        // 4. 遍历机构，匹配规则
        for (SysInstitution inst : allInsts) {
            Set<String> targetCategories = new HashSet<>();

            // A. 应用【默认规则】 (根据 instType)
            if (config.getDefaultRules() != null && config.getDefaultRules().containsKey(inst.getInstType())) {
                targetCategories.addAll(config.getDefaultRules().get(inst.getInstType()));
            }

            // B. 应用【特殊个例规则】 (根据 instCode)
            if (specialRuleMap.containsKey(inst.getInstCode())) {
                targetCategories.addAll(specialRuleMap.get(inst.getInstCode()));
            }

            // C. 构建待保存实体 (过滤掉已存在的)
            for (String categoryCode : targetCategories) {
                String uniqueKey = inst.getInstCode() + "#" + categoryCode;
                
                if (!existingRelations.contains(uniqueKey)) {
                    SysCategoryInstitutionRelation relation = new SysCategoryInstitutionRelation();
                    relation.setInstCode(inst.getInstCode());
                    relation.setCategoryCode(categoryCode);
                    entitiesToSave.add(relation);
                    
                    // 加入查重集合，防止同一次批量操作中出现重复（虽然Set已去重，但双保险）
                    existingRelations.add(uniqueKey);
                }
            }
        }

        // 5. 批量入库
        if (!entitiesToSave.isEmpty()) {
            relationRepository.saveAll(entitiesToSave);
            log.info("关联关系同步完成，新增关联 {} 条。", entitiesToSave.size());
        } else {
            log.info("关联关系已是最新，无需更新。");
        }
    }
}