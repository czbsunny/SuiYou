package com.suiyou.loader;

import com.suiyou.model.SysCategoryInstitutionRelation;
import com.suiyou.model.SysInstitution;
import com.suiyou.dto.account.RelationRuleConfigDTO;

import com.suiyou.repository.SysCategoryInstitutionRelationRepository;
import com.suiyou.repository.SysInstitutionRepository;
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
    private SysInstitutionRepository financialInstitutionRepository;

    @Autowired
    private SysCategoryInstitutionRelationRepository relationRepository;

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

        Map<String, Set<String>> categoryRules = config.getCategoryRules().entrySet().stream()
            // 把 Map 的 Entry 摊平：变成 (Category, Inst1), (Category, Inst2)... 的流
            .flatMap(entry -> entry.getValue().stream()
                .map(instCode -> new AbstractMap.SimpleEntry<>(instCode, entry.getKey())))
            // 按 InstCode 分组，把 Category 收集到 Set 中
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
            ));

        // 2. 获取数据库中所有机构
        List<SysInstitution> allInstitutions = financialInstitutionRepository.findAll();

        // 3. 获取数据库中已有的关联关系，构建查重Set (格式: "INST_CODE#CAT_CODE")
        Set<String> existingRelations = relationRepository.findAll().stream()
                .map(r -> r.getInstCode() + "#" + r.getCategoryCode())
                .collect(Collectors.toSet());

        List<SysCategoryInstitutionRelation> entitiesToSave = new ArrayList<>();

        // 4. 遍历机构，匹配规则
        for (SysInstitution institution : allInstitutions) {
            Set<String> targetCategories = new HashSet<>();

            // A. 应用【默认规则】 (根据 instType)
            if (config.getDefaultRules() != null && config.getDefaultRules().containsKey(institution.getInstType())) {
                targetCategories.addAll(config.getDefaultRules().get(institution.getInstType()));
            }

            // B. 应用【分类规则】 (根据 instCode)
            if (categoryRules.containsKey(institution.getInstCode())) {
                targetCategories.addAll(categoryRules.get(institution.getInstCode()));
            }

            // C. 应用【特殊个例规则】 (根据 instCode)
            if (specialRuleMap.containsKey(institution.getInstCode())) {
                targetCategories.addAll(specialRuleMap.get(institution.getInstCode()));
            }

            // D. 构建待保存实体 (过滤掉已存在的)
            for (String categoryCode : targetCategories) {
                String uniqueKey = institution.getInstCode() + "#" + categoryCode;
                
                if (!existingRelations.contains(uniqueKey)) {
                    SysCategoryInstitutionRelation relation = new SysCategoryInstitutionRelation();
                    relation.setInstCode(institution.getInstCode());
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


        // 6. 更新配置版本
        List<SysCategoryInstitutionRelation> sortedRelations = relationRepository.findAll(
            Sort.by(Sort.Direction.ASC, "instCode", "categoryCode")
        );
        StringBuilder sb = new StringBuilder();
        for (SysCategoryInstitutionRelation r : sortedRelations) {
            sb.append(r.getInstCode()).append(r.getCategoryCode());
        }

        updateConfigVersion("relation", DigestUtils.md5DigestAsHex(sb.toString().getBytes()));
    }
}