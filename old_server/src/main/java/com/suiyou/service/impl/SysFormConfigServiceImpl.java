package com.suiyou.service.impl;

import com.suiyou.model.SysFormConfig;
import com.suiyou.repository.SysFormConfigRepository;
import com.suiyou.service.SysFormConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SysFormConfigServiceImpl implements SysFormConfigService {
    
    @Autowired
    private SysFormConfigRepository sysFormConfigRepository;
    
    @Override
    public SysFormConfig saveOrUpdate(SysFormConfig formConfig) {
        return sysFormConfigRepository.save(formConfig);
    }
    
    @Override
    public Optional<SysFormConfig> findById(Long id) {
        return sysFormConfigRepository.findById(id);
    }
    
    @Override
    public List<SysFormConfig> findAll() {
        return sysFormConfigRepository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        sysFormConfigRepository.deleteById(id);
    }
    
    @Override
    public Long findMaxVersion() {
        return sysFormConfigRepository.findMaxVersion();
    }
    
    @Override
    public List<SysFormConfig> findByVersionGreaterThan(Long version) {
        return (List<SysFormConfig>) sysFormConfigRepository.findByVersionGreaterThan(version);
    }
}