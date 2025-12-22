package com.suiyou.service;

import java.util.List;
import java.util.Map;

import com.suiyou.dto.ConfigManifestRespDTO;

public interface ConfigManifestService {
    /**
     * 获取版本清单
     * 包含：全局 Hash + 各模块 Hash
     */
    ConfigManifestRespDTO getConfigManifest();

    /**
     * 根据 Keys 获取具体的业务数据
     * 返回 Map: Key -> 具体的业务对象(如树形结构)
     */
    Map<String, Object> fetchConfigs(List<String> keys);
}