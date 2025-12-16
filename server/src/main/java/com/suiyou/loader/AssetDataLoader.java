package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.model.SysAssetCategory;
import com.suiyou.repository.SysAssetCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Order(1)
public class AssetDataLoader implements CommandLineRunner {

    @Autowired
    private SysAssetCategoryRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:sys_asset_category_init.json")
    private Resource jsonResource;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始同步资产分类数据...");
        
        List<CategoryInitDTO> dtos = objectMapper.readValue(
            jsonResource.getInputStream(),
            new TypeReference<List<CategoryInitDTO>>() {}
        );

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
        repository.saveAll(entitiesToSave);
        
        // 可选：删除数据库中存在但JSON文件中不存在的系统分类
        // 注意：如果有用户自定义分类关联到这些系统分类，可能需要额外处理
        List<SysAssetCategory> existingSystemCategories = repository.findAllByIsSystem(true);
        List<String> existingCodes = existingSystemCategories.stream()
            .map(SysAssetCategory::getCategoryCode)
            .toList();
        
        List<String> codesToDelete = existingCodes.stream()
            .filter(code -> !expectedCodes.contains(code))
            .toList();
        
        if (!codesToDelete.isEmpty()) {
            repository.deleteByCategoryCodeIn(codesToDelete);
            log.info("删除了 {} 个不存在于JSON文件中的系统分类。", codesToDelete.size());
        }
        
        log.info("资产分类同步完成，共处理 {} 条记录。", entitiesToSave.size());
    }
    
    /**
     * 处理单个分类：如果存在则更新，不存在则创建
     */
    private SysAssetCategory processCategory(CategoryInitDTO dto, String parentCode) {
        // 根据分类code查询现有分类
        SysAssetCategory existingEntity = repository.findByCategoryCode(dto.getCategoryCode());
        
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
            // 创建新分类
            return convert(dto, parentCode);
        }
    }

    private SysAssetCategory convert(CategoryInitDTO dto, String parentCode) {
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
