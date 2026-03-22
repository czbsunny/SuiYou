package com.suiyou.loader;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.dto.account.CategoryInitDTO;
import com.suiyou.service.SysAssetConfigService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Order(5)
public class TransferCategoryDataLoader extends AbstractConfigLoader {
    @Autowired
    private SysAssetConfigService sysAssetCategoryService;

    @Value("classpath:sys_category_init.json")
    private Resource jsonResource;

    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始同步收支分类数据...");
        
        if (!jsonResource.exists()) {
            log.warn("收支分类配置文件不存在，跳过加载");
            return;
        }
        
        // 1. 读取配置
        List<CategoryInitDTO> dtos = objectMapper.readValue(
            jsonResource.getInputStream(),
            new TypeReference<List<CategoryInitDTO>>() {}
        );

        // 2. 初始化收支分类
        sysAssetCategoryService.initCategories(dtos);
        
        // 3. 更新配置版本
        updateConfigVersion("transfer_category", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(dtos)));
    }
}
