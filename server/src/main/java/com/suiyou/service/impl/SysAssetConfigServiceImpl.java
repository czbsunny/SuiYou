
package com.suiyou.service.impl;

import com.suiyou.dto.CategoryRespDTO;
import com.suiyou.dto.account.AssetCategoryRespDTO;
import com.suiyou.dto.account.InstitutionModuleRespDTO;
import com.suiyou.dto.account.InstitutionRespDTO;
import com.suiyou.dto.account.InstitutionTypeRespDTO;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.dto.account.InstitutionInitDTO;
import com.suiyou.dto.account.RelationRuleConfigDTO;
import com.suiyou.dto.account.CategoryInstitutionRelationRespDTO;
import com.suiyou.repository.SysAssetCategoryRepository;
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.repository.SysCategoryInstitutionRelationRepository;
import com.suiyou.repository.SysCategoryRepository;
import com.suiyou.repository.SysInstitutionTypeRepository;
import com.suiyou.model.SysAssetCategory;
import com.suiyou.model.SysCategoryInstitutionRelation;
import com.suiyou.model.SysInstitution;
import com.suiyou.model.SysCategory;
import com.suiyou.model.SysInstitutionType;
import com.suiyou.model.enums.AccountType;
import com.suiyou.service.SysAssetConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Autowired
    private SysCategoryRepository categoryRepository;

    @Autowired
    private SysInstitutionTypeRepository institutionTypeRepository;

    @Override
    public List<String> getAccountTypesByInstCode(String instCode) {
        SysInstitution institution = institutionRepository.findByInstCode(instCode);
        if (institution == null) {
            return null;
        }
        String instType = institution.getInstType();
        if (instType == null || instType.trim().isEmpty()) {
            return List.of();
        }
        return AccountType.getByInstitutionType(instType).stream()
                .map(at -> at.name() + ":" + at.getDescription())
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetCategoryRespDTO> getAssetCategoryTree() {
        List<SysAssetCategory> allEntities = assetCategoryRepository.findAll(Sort.by("sortOrder"));

        if (allEntities == null || allEntities.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, AssetCategoryRespDTO> lookupMap = new HashMap<>();
        
        for (SysAssetCategory entity : allEntities) {
            lookupMap.put(entity.getCategoryCode(), toAssetCategoryRespDTO(entity));
        }

        List<AssetCategoryRespDTO> rootNodes = new ArrayList<>();

        for (SysAssetCategory entity : allEntities) {
            AssetCategoryRespDTO currentVO = lookupMap.get(entity.getCategoryCode());
            String parentId = entity.getParentCode();

            if (parentId == null || parentId.isEmpty()) {
                rootNodes.add(currentVO);
            } else {
                AssetCategoryRespDTO parentVO = lookupMap.get(parentId);
                if (parentVO != null) {
                    parentVO.getChildren().add(currentVO);
                } else {
                    rootNodes.add(currentVO); 
                }
            }
        }

        return rootNodes;
    }

    private AssetCategoryRespDTO toAssetCategoryRespDTO(SysAssetCategory entity) {
        return AssetCategoryRespDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .categoryCode(entity.getCategoryCode())
                .iconUrl(entity.getIconUrl())
                .color(entity.getColor())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    @Override
    public List<InstitutionRespDTO> getAllInstitutions() {
        List<SysInstitution> institutionEntities = institutionRepository.findAllByOrderBySortOrderAsc();
        return institutionEntities.stream()
            .map(this::toInstitutionRespDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionRespDTO> getInstitutionsByType(String typeCode) {
        List<SysInstitution> institutionEntities = institutionRepository.findByInstTypeOrderBySortOrderAsc(typeCode);
        return institutionEntities.stream()
                .map(this::toInstitutionRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstitutionRespDTO> getHotInstitutions() {
        List<SysInstitution> institutionEntities = institutionRepository.findByIsHotTrueOrderBySortOrderAsc();
        return institutionEntities.stream()
                .map(this::toInstitutionRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionRespDTO getInstitutionByCode(String instCode) {
        SysInstitution entity = institutionRepository.findByInstCode(instCode);
        if (entity == null) {
            return null;
        }
        return toInstitutionRespDTO(entity);
    }

    private InstitutionTypeRespDTO toInstitutionTypeRespDTO(SysInstitutionType type) {
        List<String> accountTypes = AccountType.getByInstitutionType(type.getTypeCode())
                .stream()
                .map(at -> at.name() + ":" + at.getDescription())
                .collect(Collectors.toList());
        return InstitutionTypeRespDTO.builder()
                .id(type.getId())
                .typeCode(type.getTypeCode())
                .typeName(type.getTypeName())
                .description(type.getDescription())
                .sortOrder(type.getSortOrder())
                .iconUrl(type.getIconUrl())
                .themeColor(type.getThemeColor())
                .accountTypes(accountTypes)
                .build();
    }

    private InstitutionRespDTO toInstitutionRespDTO(SysInstitution entity) {
        InstitutionTypeRespDTO typeDto = null;
        if (entity.getInstType() != null) {
            Optional<SysInstitutionType> typeOpt = institutionTypeRepository.findByTypeCode(entity.getInstType());
            if (typeOpt.isPresent()) {
                typeDto = toInstitutionTypeRespDTO(typeOpt.get());
            }
        }

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
                .institutionType(typeDto)
                .build();
    }
    
    @Override
    public List<CategoryInstitutionRelationRespDTO> getCategoryInstitutionRelations() {
        List<SysCategoryInstitutionRelation> relationEntities = categoryInstitutionRelationRepository.findAll();
        return relationEntities.stream()
            .map(this::toCategoryInstitutionRelationRespDTO)
            .collect(Collectors.toList());
    }

    private CategoryInstitutionRelationRespDTO toCategoryInstitutionRelationRespDTO(SysCategoryInstitutionRelation entity) {
        return CategoryInstitutionRelationRespDTO.builder()
                .categoryCode(entity.getCategoryCode())
                .instCode(entity.getInstCode())
                .build();
    }

    @Override
    public InstitutionModuleRespDTO getInstitutionModules(String instCode) {
        SysInstitution institution = institutionRepository.findByInstCode(instCode);
        if (institution == null) {
            return InstitutionModuleRespDTO.builder().build();
        }

        Map<String, AssetCategoryRespDTO> categoryMap = assetCategoryRepository.findAll().stream()
            .collect(Collectors.toMap(SysAssetCategory::getCategoryCode, this::toAssetCategoryRespDTO));

        List<SysCategoryInstitutionRelation> relations = categoryInstitutionRelationRepository.findByInstCode(instCode);

        List<AssetCategoryRespDTO> requiredList = new ArrayList<>();
        List<AssetCategoryRespDTO> defaultList = new ArrayList<>();
        List<AssetCategoryRespDTO> optionalList = new ArrayList<>();

        for (SysCategoryInstitutionRelation relation : relations) {
            AssetCategoryRespDTO categoryDTO = categoryMap.get(relation.getCategoryCode());
            if (categoryDTO != null) {
                switch (relation.getRelationType()) {
                    case "REQUIRED":
                        requiredList.add(categoryDTO);
                        break;
                    case "DEFAULT":
                        defaultList.add(categoryDTO);
                        break;
                    case "OPTIONAL":
                        optionalList.add(categoryDTO);
                        break;
                }
            }
        }

        requiredList.sort(Comparator.comparingInt(AssetCategoryRespDTO::getSortOrder));
        defaultList.sort(Comparator.comparingInt(AssetCategoryRespDTO::getSortOrder));
        optionalList.sort(Comparator.comparingInt(AssetCategoryRespDTO::getSortOrder));

        return InstitutionModuleRespDTO.builder()
                .required(requiredList)
                .defaultList(defaultList)
                .optional(optionalList)
                .build();
    }
    
    @Override
    public List<CategoryRespDTO> getTransferCategoryTree() {
        List<SysCategory> allEntities = categoryRepository.findAll(Sort.by("sortOrder"));

        if (allEntities == null || allEntities.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, CategoryRespDTO> lookupMap = new HashMap<>();
        
        for (SysCategory entity : allEntities) {
            lookupMap.put(entity.getCategoryCode(), toCategoryRespDTO(entity));
        }

        List<CategoryRespDTO> rootNodes = new ArrayList<>();

        for (SysCategory entity : allEntities) {
            CategoryRespDTO currentVO = lookupMap.get(entity.getCategoryCode());
            String parentId = entity.getParentCode();
            if (parentId == null || parentId.isEmpty()) {
                rootNodes.add(currentVO);
            } else {
                CategoryRespDTO parentVO = lookupMap.get(parentId);
                if (parentVO != null) {
                    parentVO.getChildren().add(currentVO);
                } else {
                    rootNodes.add(currentVO); 
                }
            }
        }

        return rootNodes;
    }

    private CategoryRespDTO toCategoryRespDTO(SysCategory entity) {
        return CategoryRespDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .categoryCode(entity.getCategoryCode())
                .iconUrl(entity.getIconUrl())
                .groupType(entity.getGroupType())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    @Override
    @Transactional
    public void initAssetCategories(List<CategoryInitDTO> dtos) {
        List<String> expectedCodes = new ArrayList<>();
        List<SysAssetCategory> entitiesToSave = new ArrayList<>();

        for (CategoryInitDTO parent : dtos) {
            expectedCodes.add(parent.getCategoryCode());
            SysAssetCategory parentEntity = processAssetCategory(parent, null);
            entitiesToSave.add(parentEntity);

            if (parent.getChildren() != null) {
                for (CategoryInitDTO child : parent.getChildren()) {
                    if (child.getGroupType() == null) {
                        child.setGroupType(parent.getGroupType());
                    }
                    expectedCodes.add(child.getCategoryCode());
                    SysAssetCategory childEntity = processAssetCategory(child, parent.getCategoryCode());
                    entitiesToSave.add(childEntity);
                }
            }
        }

        assetCategoryRepository.saveAll(entitiesToSave);
        
        List<SysAssetCategory> existingSystemCategories = assetCategoryRepository.findAllByIsSystem(true);
        List<String> existingCodes = existingSystemCategories.stream()
            .map(SysAssetCategory::getCategoryCode)
            .collect(Collectors.toList());
        
        List<String> codesToDelete = existingCodes.stream()
            .filter(code -> !expectedCodes.contains(code))
            .collect(Collectors.toList());
        
        if (!codesToDelete.isEmpty()) {
            categoryInstitutionRelationRepository.deleteByCategoryCodeIn(codesToDelete);
            assetCategoryRepository.deleteByCategoryCodeIn(codesToDelete);
            log.info("删除了 {} 个不存在于JSON文件中的系统分类及其关联关系。", codesToDelete.size());
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

        institutionRepository.saveAll(entitiesToSave);
    }

    @Override
    @Transactional
    public void initCategoryInstitutionRelations(RelationRuleConfigDTO config) {
        log.info("正在同步 [资产分类与机构关联关系] 数据...");

        Map<String, Set<String>> categoryRules = new HashMap<>();
        if (config.getCategoryRules() != null) {
            categoryRules = config.getCategoryRules().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                    .map(instCode -> new AbstractMap.SimpleEntry<>(instCode, entry.getKey())))
                .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
        }

        List<SysInstitution> allInstitutions = institutionRepository.findAll();
        
        categoryInstitutionRelationRepository.deleteAllInBatch();

        Set<String> addedRelations = new HashSet<>();
        List<SysCategoryInstitutionRelation> entitiesToSave = new ArrayList<>();

        for (SysInstitution institution : allInstitutions) {
            RelationRuleConfigDTO.ModuleRuleDTO instRule = config.getInstRules() != null ? config.getInstRules().get(institution.getInstCode()) : null;
            RelationRuleConfigDTO.ModuleRuleDTO typeRule = config.getTypeRules() != null ? config.getTypeRules().get(institution.getInstType()) : null;

            RelationRuleConfigDTO.ModuleRuleDTO effectiveRule = instRule != null ? instRule : typeRule;

            if (effectiveRule != null) {
                if (effectiveRule.getRequired() != null) {
                    for (String categoryCode : effectiveRule.getRequired()) {
                        String key = institution.getInstCode() + "#" + categoryCode;
                        if (!addedRelations.contains(key)) {
                            entitiesToSave.add(createRelation(institution.getInstCode(), categoryCode, "REQUIRED"));
                            addedRelations.add(key);
                        }
                    }
                }

                if (effectiveRule.getDefaultList() != null) {
                    for (String categoryCode : effectiveRule.getDefaultList()) {
                        String key = institution.getInstCode() + "#" + categoryCode;
                        if (!addedRelations.contains(key)) {
                            entitiesToSave.add(createRelation(institution.getInstCode(), categoryCode, "DEFAULT"));
                            addedRelations.add(key);
                        }
                    }
                }

                if (effectiveRule.getOptional() != null) {
                    for (String categoryCode : effectiveRule.getOptional()) {
                        String key = institution.getInstCode() + "#" + categoryCode;
                        if (!addedRelations.contains(key)) {
                            entitiesToSave.add(createRelation(institution.getInstCode(), categoryCode, "OPTIONAL"));
                            addedRelations.add(key);
                        }
                    }
                }
            }

            if (categoryRules.containsKey(institution.getInstCode())) {
                for (String categoryCode : categoryRules.get(institution.getInstCode())) {
                    String key = institution.getInstCode() + "#" + categoryCode;
                    if (!addedRelations.contains(key)) {
                        entitiesToSave.add(createRelation(institution.getInstCode(), categoryCode, "OPTIONAL"));
                        addedRelations.add(key);
                    }
                }
            }
        }

        if (!entitiesToSave.isEmpty()) {
            categoryInstitutionRelationRepository.saveAll(entitiesToSave);
            log.info("关联关系同步完成，共保存 {} 条。", entitiesToSave.size());
        } else {
            log.info("没有关联关系需要保存。");
        }
    }

    private SysCategoryInstitutionRelation createRelation(String instCode, String categoryCode, String relationType) {
        SysCategoryInstitutionRelation relation = new SysCategoryInstitutionRelation();
        relation.setInstCode(instCode);
        relation.setCategoryCode(categoryCode);
        relation.setRelationType(relationType);
        return relation;
    }

    private SysAssetCategory processAssetCategory(CategoryInitDTO dto, String parentCode) {
        SysAssetCategory existingEntity = assetCategoryRepository.findByCategoryCode(dto.getCategoryCode());
        
        if (existingEntity != null) {
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

    private SysInstitution processInstitution(InstitutionInitDTO dto) {
        SysInstitution existingEntity = institutionRepository.findByInstCode(dto.getInstCode());
        String indexLetter = null;
        if (dto.getShortName() != null && !dto.getShortName().trim().isEmpty()) {
            indexLetter = getChineseInitialLetters(dto.getShortName());
        }
        
        if (existingEntity != null) {
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

    private String getChineseInitialLetters(String chineseStr) {
        if (chineseStr == null || chineseStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(net.sourceforge.pinyin4j.format.HanyuPinyinCaseType.UPPERCASE);
            format.setToneType(net.sourceforge.pinyin4j.format.HanyuPinyinToneType.WITHOUT_TONE);
            
            char[] chars = chineseStr.toCharArray();
            
            for (char c : chars) {
                if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        return String.valueOf(pinyinArray[0].charAt(0));
                    }
                } else if (Character.isLetter(c)) {
                    return String.valueOf(Character.toUpperCase(c));
                }
            }
            
            return "#";
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.warn("拼音转换失败: {}, 错误: {}", chineseStr, e.getMessage());
            return chineseStr.substring(0, 1).toUpperCase();
        }
    }

    @Override
    @Transactional
    public void initCategories(List<CategoryInitDTO> dtos) {
        List<String> expectedCodes = new ArrayList<>();
        List<SysCategory> entitiesToSave = new ArrayList<>();

        for (CategoryInitDTO parent : dtos) {
            expectedCodes.add(parent.getCategoryCode());
            SysCategory parentEntity = processCategory(parent, null);
            entitiesToSave.add(parentEntity);

            if (parent.getChildren() != null) {
                for (CategoryInitDTO child : parent.getChildren()) {
                    if (child.getGroupType() == null) {
                        child.setGroupType(parent.getGroupType());
                    }
                    expectedCodes.add(child.getCategoryCode());
                    SysCategory childEntity = processCategory(child, parent.getCategoryCode());
                    entitiesToSave.add(childEntity);
                }
            }
        }

        categoryRepository.saveAll(entitiesToSave);

        List<SysCategory> existingSystemCategories = categoryRepository.findAllByIsSystem(true);
        List<String> existingCodes = existingSystemCategories.stream()
            .map(SysCategory::getCategoryCode)
            .collect(Collectors.toList());
        
        List<String> codesToDelete = existingCodes.stream()
            .filter(code -> !expectedCodes.contains(code))
            .collect(Collectors.toList());
        
        if (!codesToDelete.isEmpty()) {
            categoryRepository.deleteByCategoryCodeIn(codesToDelete);
            log.info("删除了 {} 个不存在于JSON文件中的系统分类。", codesToDelete.size());
        }

        log.info("资产分类同步完成，共处理 {} 条记录。", entitiesToSave.size());
    }

    private SysCategory processCategory(CategoryInitDTO dto, String parentCode) {
        SysCategory existingEntity = categoryRepository.findByCategoryCode(dto.getCategoryCode());
        
        if (existingEntity != null) {
            existingEntity.setName(dto.getName());
            existingEntity.setIconUrl(dto.getIconUrl());
            existingEntity.setGroupType(dto.getGroupType());
            existingEntity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            existingEntity.setParentCode(parentCode);
            existingEntity.setDescription(dto.getDescription());
            existingEntity.setIsSystem(true);
            return existingEntity;
        } else {
            SysCategory entity = new SysCategory();
            entity.setCategoryCode(dto.getCategoryCode());
            entity.setName(dto.getName());
            entity.setIconUrl(dto.getIconUrl());
            entity.setGroupType(dto.getGroupType());
            entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            entity.setParentCode(parentCode);
            entity.setDescription(dto.getDescription());
            entity.setIsSystem(true);
            return entity;
        }
    }
}
