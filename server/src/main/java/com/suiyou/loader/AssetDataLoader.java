package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.service.SysAssetCategoryService;

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
    private SysAssetCategoryService sysAssetCategoryService;

    @Value("classpath:sys_asset_category_init.json")
    private Resource jsonResource;

    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始同步资产分类数据...");
        
        List<CategoryInitDTO> dtos = objectMapper.readValue(
            jsonResource.getInputStream(),
            new TypeReference<List<CategoryInitDTO>>() {}
        );

        sysAssetCategoryService.syncFromLoader(dtos);
        
        updateConfigVersion("asset_category", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(dtos)));
    }
}