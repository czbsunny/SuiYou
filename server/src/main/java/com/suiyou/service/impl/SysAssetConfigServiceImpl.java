package com.suiyou.service.impl;

import com.suiyou.dto.account.AssetCategoryRespDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.dto.account.InstitutionInitDTO;
import com.suiyou.dto.account.RelationRuleConfigDTO;
import com.suiyou.dto.account.CategoryInstitutionRelationRespDTO;

import com.suiyou.repository.SysAssetCategoryRepository;
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.repository.SysCategoryInstitutionRelationRepository;

import com.suiyou.model.SysAssetCategory;
import com.suiyou.model.SysCategoryInstitutionRelation;
import com.suiyou.model.SysInstitution;

import com.suiyou.service.SysAssetConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

// pinyin4j 导入
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysAssetConfigServiceImpl implements SysAssetConfigService {
    @Resource
    private SysAssetCategoryRepository assetCategoryRepository;

    @Autowired
    private SysInstitutionRepository institutionRepository;

    @Autowired
    private SysCategoryInstitutionRelationRepository categoryInstitutionRelationRepository;

    @Override
    public List<AssetCategoryRespDTO> getCategoryTree() {
        // 1. 查出所有数据 (关键：先按 sort_order 排序，这样生成的树也是有序的)
        // 如果是 MyBatis Plus: new QueryWrapper<>().orderByAsc("sort_order")
        List<SysAssetCategory> allEntities = assetCategoryRepository.findAll(Sort.by("sortOrder"));

        if (allEntities == null || allEntities.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 准备一个临时 Map 用于快速查找 (Key: ID, Value: VO)
        // 这一步的时间复杂度是 O(N)
        Map<String, AssetCategoryRespDTO> lookupMap = new HashMap<>();
        
        // 转换 Entity -> VO 并放入 Map
        for (SysAssetCategory entity : allEntities) {
            lookupMap.put(entity.getCategoryCode(), toCategoryRespDTO(entity));
        }

        // 3. 组装树 (再遍历一次，O(N))
        List<AssetCategoryRespDTO> rootNodes = new ArrayList<>();

        for (SysAssetCategory entity : allEntities) {
            AssetCategoryRespDTO currentVO = lookupMap.get(entity.getCategoryCode());
            String parentId = entity.getParentCode();

            // 判断逻辑：如果没有父ID，或者父ID为0，视为根节点
            if (parentId == null || parentId.isEmpty()) {
                rootNodes.add(currentVO);
            } else {
                // 如果有父节点，从 Map 中找到父亲，把自己加进去
                AssetCategoryRespDTO parentVO = lookupMap.get(parentId);
                if (parentVO != null) {
                    parentVO.getChildren().add(currentVO);
                } else {
                    // 边界情况处理：如果数据库脏数据导致找不到爹，通常作为一个根节点处理，或者忽略
                    // 这里选择作为根节点兜底，防止数据丢失
                    rootNodes.add(currentVO); 
                }
            }
        }

        return rootNodes;
    }

    /**
     * 辅助方法：Entity 转 VO
     * 实际项目中可以使用 MapStruct 或 BeanUtils 简化
     */
    private AssetCategoryRespDTO toCategoryRespDTO(SysAssetCategory entity) {
        return AssetCategoryRespDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .categoryCode(entity.getCategoryCode())
                .iconUrl(entity.getIconUrl())
                .color(entity.getColor())
                .sortOrder(entity.getSortOrder())
                .build();
    }

     /**
     * 获取所有资产机构
     */
    @Override
    public List<InstitutionRespDTO> getAllInstitutions() {
        // 从数据库查询所有机构
        List<SysInstitution> institutionEntities = institutionRepository.findAll();
        
        // 转换为 DTO 列表
        return institutionEntities.stream()
            .map(this::toInstitutionRespDTO)
            .collect(Collectors.toList());
    }

    /**
     * 辅助方法：Entity 转 InstitutionInitDTO
     */
    private InstitutionRespDTO toInstitutionRespDTO(SysInstitution entity) {
        return InstitutionRespDTO.builder()
                .id(entity.getId())
                .instCode(entity.getInstCode())
                .instName(entity.getInstName())
                .shortName(entity.getShortName())
                .instType(entity.getInstType())
                .logoUrl(entity.getLogoUrl())
                .themeColor(entity.getThemeColor())
                .indexLetter(entity.getIndexLetter())
                .isHot(entity.getIsHot())
                .build();
    }
    
     /**
     * 获取资产分类与机构的关联关系
     */
    @Override
    public List<CategoryInstitutionRelationRespDTO> getCategoryInstitutionRelations() {
        // 从数据库查询所有关联关系
        List<SysCategoryInstitutionRelation> relationEntities = categoryInstitutionRelationRepository.findAll();
        
        // 转换为 DTO 列表
        return relationEntities.stream()
            .map(this::toCategoryInstitutionRelationRespDTO)
            .collect(Collectors.toList());
    }

    /**
     * 辅助方法：Entity 转 CategoryInstitutionRelationRespDTO
     */
    private CategoryInstitutionRelationRespDTO toCategoryInstitutionRelationRespDTO(SysCategoryInstitutionRelation entity) {
        return CategoryInstitutionRelationRespDTO.builder()
                .categoryCode(entity.getCategoryCode())
                .instCode(entity.getInstCode())
                .build();
    }
    
    /**
     * 供 DataLoader 使用的方法 (之前提到的)
     */
    @Override
    public void initCategories(List<CategoryInitDTO> dtos) {
         // 收集所有需要存在的分类code
        List<String> expectedCodes = new ArrayList<>();
        List<SysAssetCategory> entitiesToSave = new ArrayList<>();

        // 处理一级分类和二级分类
        for (CategoryInitDTO parent : dtos) {
            // 添加一级分类code
            expectedCodes.add(parent.getCategoryCode());
            
            // 转换并保存一级分类
            SysAssetCategory parentEntity = processCategory(parent, null);
            entitiesToSave.add(parentEntity);

            // 处理二级分类
            if (parent.getChildren() != null) {
                for (CategoryInitDTO child : parent.getChildren()) {
                    if (child.getGroupType() == null) {
                        child.setGroupType(parent.getGroupType());
                    }
                    // 添加二级分类code
                    expectedCodes.add(child.getCategoryCode());
                    
                    // 转换并保存二级分类
                    SysAssetCategory childEntity = processCategory(child, parent.getCategoryCode());
                    entitiesToSave.add(childEntity);
                }
            }
        }

        // 保存所有更新或新增的分类
        assetCategoryRepository.saveAll(entitiesToSave);
        
        // 注意：如果有用户自定义分类关联到这些系统分类，可能需要额外处理
        List<SysAssetCategory> existingSystemCategories = assetCategoryRepository.findAllByIsSystem(true);
        List<String> existingCodes = existingSystemCategories.stream()
            .map(SysAssetCategory::getCategoryCode)
            .toList();
        
        List<String> codesToDelete = existingCodes.stream()
            .filter(code -> !expectedCodes.contains(code))
            .toList();
        
        if (!codesToDelete.isEmpty()) {
            assetCategoryRepository.deleteByCategoryCodeIn(codesToDelete);
            log.info("删除了 {} 个不存在于JSON文件中的系统分类。", codesToDelete.size());
        }

        log.info("资产分类同步完成，共处理 {} 条记录。", entitiesToSave.size());
    }

    @Override
    public void initInstitutions(List<InstitutionInitDTO> dtos) {
        log.info("正在同步 [金融机构] 数据...");

        List<String> expectedCodes = new ArrayList<>();
        List<SysInstitution> entitiesToSave = new ArrayList<>();

        Set<String> instCodeSet = new java.util.HashSet<>();
        for (InstitutionInitDTO dto : dtos) {
            if (!instCodeSet.add(dto.getInstCode())) {
                log.warn("发现重复的instCode: {}", dto.getInstCode());
                continue;
            }
            expectedCodes.add(dto.getInstCode());
            entitiesToSave.add(processInstitution(dto));
        }

        // 保存所有更新或新增的机构
        institutionRepository.saveAll(entitiesToSave);
    }

    @Override
    public void initCategoryInstitutionRelations(RelationRuleConfigDTO config) {
        log.info("正在同步 [资产分类与机构关联关系] 数据...");

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
        List<SysInstitution> allInstitutions = institutionRepository.findAll();

        // 3. 获取数据库中已有的关联关系，构建查重Set (格式: "INST_CODE#CAT_CODE")
        Set<String> existingRelations = categoryInstitutionRelationRepository.findAll().stream()
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
            categoryInstitutionRelationRepository.saveAll(entitiesToSave);
            log.info("关联关系同步完成，新增关联 {} 条。", entitiesToSave.size());
        } else {
            log.info("关联关系已是最新，无需更新。");
        }


        // 6. 更新配置版本
        List<SysCategoryInstitutionRelation> sortedRelations = categoryInstitutionRelationRepository.findAll(
            Sort.by(Sort.Direction.ASC, "instCode", "categoryCode")
        );
        StringBuilder sb = new StringBuilder();
        for (SysCategoryInstitutionRelation r : sortedRelations) {
            sb.append(r.getInstCode()).append(r.getCategoryCode());
        }
    }


    /**
     * 处理单个分类：如果存在则更新，不存在则创建
     */
    private SysAssetCategory processCategory(CategoryInitDTO dto, String parentCode) {
        // 根据分类code查询现有分类
        SysAssetCategory existingEntity = assetCategoryRepository.findByCategoryCode(dto.getCategoryCode());
        
        if (existingEntity != null) {
            // 更新现有分类的属性
            existingEntity.setName(dto.getName());
            existingEntity.setGroupType(dto.getGroupType());
            existingEntity.setIconUrl(dto.getIconUrl());
            existingEntity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            existingEntity.setColor(dto.getColor());
            existingEntity.setParentCode(parentCode);
            existingEntity.setDescription(dto.getDescription());
            existingEntity.setIsSystem(true);
            return existingEntity;
        } else {
            SysAssetCategory entity = new SysAssetCategory();
            entity.setCategoryCode(dto.getCategoryCode());
            entity.setName(dto.getName());
            entity.setGroupType(dto.getGroupType());
            entity.setIconUrl(dto.getIconUrl());
            entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            entity.setColor(dto.getColor());
            entity.setParentCode(parentCode);
            entity.setDescription(dto.getDescription());
            entity.setIsSystem(true);
            return entity;
        }
    }

    /**
     * 处理单个金融机构：如果存在则更新，不存在则创建
     */
    private SysInstitution processInstitution(InstitutionInitDTO dto) {
        // 根据机构code查询现有机构
        SysInstitution existingEntity = institutionRepository.findByInstCode(dto.getInstCode());
        
        // 自动生成首字母（如果提供了shortName）
        String indexLetter = null;
        if (dto.getShortName() != null && !dto.getShortName().trim().isEmpty()) {
            indexLetter = getChineseInitialLetters(dto.getShortName());
        }
        
        if (existingEntity != null) {
            // 更新现有机构的属性
            existingEntity.setInstName(dto.getInstName());
            existingEntity.setShortName(dto.getShortName());
            existingEntity.setInstType(dto.getInstType());
            existingEntity.setLogoUrl(dto.getLogoUrl());
            existingEntity.setThemeColor(dto.getThemeColor());
            existingEntity.setIndexLetter(indexLetter);
            existingEntity.setSortOrder(dto.getSortOrder());
            existingEntity.setIsHot(Optional.ofNullable(dto.getIsHot()).orElse(false));
            return existingEntity;
        } else {
            SysInstitution entity = new SysInstitution();
            entity.setInstCode(dto.getInstCode());
            entity.setInstName(dto.getInstName());
            entity.setShortName(dto.getShortName());
            entity.setInstType(dto.getInstType());
            entity.setLogoUrl(dto.getLogoUrl());
            entity.setThemeColor(dto.getThemeColor());
            entity.setIndexLetter(indexLetter);
            entity.setSortOrder(dto.getSortOrder());
            entity.setIsHot(Optional.ofNullable(dto.getIsHot()).orElse(false));
            return entity;
        }
    }

    /**
     * 获取中文字符串的首字母
     * @param chineseStr 中文字符串
     * @return 首字母字符串（如：工商银行 -> G）
     */
    private String getChineseInitialLetters(String chineseStr) {
        if (chineseStr == null || chineseStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            // 设置格式为大写首字母
            format.setCaseType(net.sourceforge.pinyin4j.format.HanyuPinyinCaseType.UPPERCASE);
            format.setToneType(net.sourceforge.pinyin4j.format.HanyuPinyinToneType.WITHOUT_TONE);
            
            char[] chars = chineseStr.toCharArray();
            
            for (char c : chars) {
                if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) { // 中文字符
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        return String.valueOf(pinyinArray[0].charAt(0)); // 返回第一个中文字符的首字母
                    }
                } else if (Character.isLetter(c)) { // 英文字母
                    return String.valueOf(Character.toUpperCase(c)); // 返回第一个英文字母
                }
                // 跳过数字和特殊字符，继续查找下一个字符
            }
            
            // 如果没有找到任何字母，返回#
            return "#";
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.warn("拼音转换失败: {}, 错误: {}", chineseStr, e.getMessage());
            // 如果拼音转换失败，返回第一个字符
            return chineseStr.substring(0, 1).toUpperCase();
        }
    }
}