package com.suiyou.controller;

import com.suiyou.model.SysFormConfig;
import com.suiyou.service.SysFormConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sys/configs")
public class SysFormConfigController {
    
    @Autowired
    private SysFormConfigService sysFormConfigService;
    
    /**
     * 获取所有表单配置
     * @return 表单配置列表
     */
    @GetMapping
    public ResponseEntity<List<SysFormConfig>> getAllFormConfigs() {
        List<SysFormConfig> formConfigs = sysFormConfigService.findAll();
        return new ResponseEntity<>(formConfigs, HttpStatus.OK);
    }
    
    /**
     * 根据ID获取表单配置
     * @param id 表单配置ID
     * @return 表单配置对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<SysFormConfig> getFormConfigById(@PathVariable Long id) {
        Optional<SysFormConfig> formConfig = sysFormConfigService.findById(id);
        return formConfig.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
        
    /**
     * 查询当前最大的版本号
     * @return 最大版本号
     */
    @GetMapping("/version")
    public ResponseEntity<Long> findMaxVersion() {
        Long maxVersion = sysFormConfigService.findMaxVersion();
        return new ResponseEntity<>(maxVersion, HttpStatus.OK);
    }
    
    /**
     * 获取大于指定版本号的数据
     * @param clientVersion 指定版本号
     * @return 表单配置列表
     */
    @GetMapping("/sync/{clientVersion}")
    public ResponseEntity<List<SysFormConfig>> findByVersionGreaterThan(@PathVariable Long clientVersion) {
        List<SysFormConfig> formConfigs = sysFormConfigService.findByVersionGreaterThan(clientVersion);
        return new ResponseEntity<>(formConfigs, HttpStatus.OK);
    }
}