package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.service.SysAssetConfigService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;

@Component
@Slf4j
@Order(1)
public class AssetDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysAssetConfigService sysAssetCategoryService;

    @Value("classpath:sys_asset_category_init.json")
    private Resource jsonResource;

    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始同步资产分类数据...");
        
        if (!jsonResource.exists()) {
            log.warn("资产分类配置文件不存在，跳过加载");
            return;
        }
        
        // 1. 读取配置
        List<CategoryInitDTO> dtos = objectMapper.readValue(
            jsonResource.getInputStream(),
            new TypeReference<List<CategoryInitDTO>>() {}
        );

        // 2. 初始化资产分类
        sysAssetCategoryService.initCategories(dtos);
        
        // 3. 更新配置版本
        updateConfigVersion("asset_category", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(dtos)));
    }
}