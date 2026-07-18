package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.model.SysGoalTemplate;
import com.suiyou.repository.SysGoalTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;

@Component
@Order(4)
@lombok.extern.slf4j.Slf4j
public class GoalTemplateDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysGoalTemplateRepository sysGoalTemplateRepository;

    @Value("classpath:sys_goal_template_init.json")
    private Resource jsonResource;

    @Override
    protected void loadConfig() throws Exception {
        if (!jsonResource.exists()) {
            log.warn("目标模板配置文件不存在，跳过加载");
            return;
        }

        List<SysGoalTemplate> templates = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(),
            new TypeReference<List<SysGoalTemplate>>() {}
        );

        sysGoalTemplateRepository.deleteAll();
        sysGoalTemplateRepository.saveAll(templates);

        updateConfigVersion("goal_template_data", 
            DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(templates)));

        log.info("目标模板数据同步完成，共 {} 条记录", templates.size());
    }
    
    @Override
    protected String getLoaderName() {
        return "目标模板数据加载器";
    }
}