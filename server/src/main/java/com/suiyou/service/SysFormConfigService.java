package com.suiyou.service;

import com.suiyou.model.SysFormConfig;

import java.util.List;
import java.util.Optional;

public interface SysFormConfigService {
    
    /**
     * 保存或更新表单配置
     * @param formConfig 表单配置对象
     * @return 保存后的表单配置对象
     */
    SysFormConfig saveOrUpdate(SysFormConfig formConfig);
    
    /**
     * 根据ID查询表单配置
     * @param id 表单配置ID
     * @return 表单配置对象
     */
    Optional<SysFormConfig> findById(Long id);
    
    /**
     * 查询所有表单配置
     * @return 表单配置列表
     */
    List<SysFormConfig> findAll();
    
    /**
     * 根据ID删除表单配置
     * @param id 表单配置ID
     */
    void deleteById(Long id);
    
    /**
     * 查询当前最大的版本号
     * @return 最大版本号
     */
    Long findMaxVersion();
    
    /**
     * 获取大于指定版本号的数据
     * @param version 指定版本号
     * @return 表单配置列表
     */
    List<SysFormConfig> findByVersionGreaterThan(Long version);
}