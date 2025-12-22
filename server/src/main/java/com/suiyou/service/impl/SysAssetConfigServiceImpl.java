package com.suiyou.service.impl;

import com.suiyou.dto.account.AssetCategoryRespDTO;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.dto.account.InstitutionInitDTO;
import com.suiyou.repository.SysAssetCategoryRepository;
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.model.SysAssetCategory;
import com.suiyou.model.SysInstitution;
import com.suiyou.service.SysAssetConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class SysAssetConfigServiceImpl implements SysAssetConfigService {

    @Resource
    private SysAssetCategoryRepository assetCategoryRepository;

    @Autowired
    private SysInstitutionRepository institutionRepository;

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
            lookupMap.put(entity.getCategoryCode(), toRespDTO(entity));
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
    private AssetCategoryRespDTO toRespDTO(SysAssetCategory entity) {
        return AssetCategoryRespDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCategoryCode())
                .icon(entity.getIconUrl())
                // .formKey(entity.getFormKey()) // 假设实体里有这个字段
                .sortOrder(entity.getSortOrder())
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
        
        if (existingEntity != null) {
            // 更新现有机构的属性
            existingEntity.setInstName(dto.getInstName());
            existingEntity.setLogoUrl(dto.getLogoUrl());
            existingEntity.setSortOrder(dto.getSortOrder());
            return existingEntity;
        } else {
            SysInstitution entity = new SysInstitution();
            entity.setInstCode(dto.getInstCode());
            entity.setInstName(dto.getInstName());
            entity.setLogoUrl(dto.getLogoUrl());
            entity.setSortOrder(dto.getSortOrder());
            return entity;
        }
    }
}