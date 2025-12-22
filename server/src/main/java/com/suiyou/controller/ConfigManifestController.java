package com.suiyou.controller;

import com.suiyou.service.ConfigManifestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.suiyou.dto.ConfigManifestRespDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigManifestController {

    @Autowired
    private ConfigManifestService configManifestService;

    /**
     * 获取配置清单
     * @return 包含全局指纹和模块版本详情的响应
     */
    @GetMapping("/manifest")
    public ResponseEntity<ConfigManifestRespDTO> getConfigManifest() {
        return new ResponseEntity<>(configManifestService.getConfigManifest(), HttpStatus.OK);
    }

    /**
     * 根据keys获取配置数据
     * @param requestBody 包含keys的请求体
     * @return 包含指定配置数据的响应
     */
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchConfigs(@RequestBody List<String> keys) {
        return new ResponseEntity<>(configManifestService.fetchConfigs(keys), HttpStatus.OK);
    }
}