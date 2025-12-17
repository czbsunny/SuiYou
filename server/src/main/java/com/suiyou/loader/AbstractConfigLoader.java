package com.suiyou.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.repository.SysConfigVersionRepository;
import com.suiyou.model.SysConfigVersion;



public abstract class AbstractConfigLoader implements CommandLineRunner {
    @Autowired
    private SysConfigVersionRepository versionRepository;

    protected final ObjectMapper objectMapper = new ObjectMapper();
    
    protected abstract void loadConfig() throws Exception;
    
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
    public void run(String... args) throws Exception {
        loadConfig();
    }
}
