package com.suiyou.service;

import java.util.List;
import java.util.Map;

import com.suiyou.model.SysConfigVersion;

public interface ConfigManifestService {
    /**
     * 获取配置清单
     * @return 包含全局指纹和模块版本详情的Map
     */
    Map<String, Object> getConfigManifest();

    /**
     * 根据keys获取配置数据
     * @param keys 配置键列表
     * @return 包含指定配置数据的Map
     */
    Map<String, Object> fetchConfigs(List<String> keys);

    /**
     * 获取所有模块版本，按模块键升序排序
     * @return 包含所有模块版本的列表
     */
    List<SysConfigVersion> getAllVersionsSorted();
}