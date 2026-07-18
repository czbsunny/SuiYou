package com.suiyou.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.suiyou.model.SysConfigVersion;
import com.suiyou.repository.SysConfigVersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

@Slf4j
public abstract class AbstractConfigLoader implements CommandLineRunner {
    @Autowired
    private SysConfigVersionRepository versionRepository;

    protected final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    protected abstract void loadConfig() throws Exception;
    
    protected abstract String getLoaderName();
    
    protected void updateConfigVersion(String moduleKey, String newHash) {
        SysConfigVersion existing = versionRepository.findByModuleKey(moduleKey)
                .orElse(null);
        
        if (existing == null || !existing.getVersionHash().equals(newHash)) {
            SysConfigVersion version = new SysConfigVersion();
            version.setModuleKey(moduleKey);
            version.setVersionHash(newHash);
            versionRepository.save(version);
        }
    }
    
    @Override
    public void run(String... args) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("开始执行 {}...", getLoaderName());
            loadConfig();
            long endTime = System.currentTimeMillis();
            log.info("{} 执行完成，耗时 {} ms", getLoaderName(), endTime - startTime);
        } catch (Exception e) {
            log.error("{} 执行失败: {}", getLoaderName(), e.getMessage(), e);
        }
    }
}