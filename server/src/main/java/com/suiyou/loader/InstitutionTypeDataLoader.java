
package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.dto.account.InstitutionTypeInitDTO;
import com.suiyou.service.SysInstitutionTypeService;
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
@Order(1)
@Slf4j
public class InstitutionTypeDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysInstitutionTypeService institutionTypeService;

    @Value("classpath:sys_institution_type_init.json")
    private Resource jsonResource;

    @Override
    @Transactional
    protected void loadConfig() throws Exception {
        log.info("开始同步机构类型数据...");

        // 解析JSON数据
        if (!jsonResource.exists()) {
            log.warn("机构类型配置文件不存在，跳过加载");
            return;
        }

        // 1. 读取配置
        List&lt;InstitutionTypeInitDTO&gt; dtos = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(),
            new TypeReference&lt;List&lt;InstitutionTypeInitDTO&gt;&gt;() {}
        );

        // 2. 初始化机构类型
        institutionTypeService.initInstitutionTypes(dtos);

        // 3. 更新配置版本
        updateConfigVersion("institution_type_data", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(dtos)));

        log.info("机构类型数据同步完成。");
    }
}
