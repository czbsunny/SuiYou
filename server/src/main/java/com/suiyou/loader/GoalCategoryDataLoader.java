package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.model.SysGoalCategory;
import com.suiyou.repository.SysGoalCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;

@Component
@Order(3)
@lombok.extern.slf4j.Slf4j
public class GoalCategoryDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysGoalCategoryRepository sysGoalCategoryRepository;

    @Value("classpath:sys_goal_category_init.json")
    private Resource jsonResource;

    @Override
    protected void loadConfig() throws Exception {
        if (!jsonResource.exists()) {
            log.warn("目标分类配置文件不存在，跳过加载");
            return;
        }

        List<SysGoalCategory> categories = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(),
            new TypeReference<List<SysGoalCategory>>() {}
        );

        sysGoalCategoryRepository.deleteAll();
        sysGoalCategoryRepository.saveAll(categories);

        updateConfigVersion("goal_category_data", 
            DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(categories)));

        log.info("目标分类数据同步完成，共 {} 条记录", categories.size());
    }
    
    @Override
    protected String getLoaderName() {
        return "目标分类数据加载器";
    }
}