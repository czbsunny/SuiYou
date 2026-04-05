package com.suiyou.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * DataServer配置类，集中管理与data_server的连接配置
 */
@Configuration
public class DataServerConfig {

    @Value("${data.server.scheme:http}")
    private String scheme;

    @Value("${data.server.host:localhost}")
    private String host;

    @Value("${data.server.port:7575}")
    private int port;

    /**
     * 获取完整的基础URL
     * @return 基础URL，格式为scheme://host:port
     */
    public String getBaseUrl() {
        return String.format("%s://%s:%d", scheme, host, port);
    }

    /**
     * 获取API完整URL
     * @param apiPath API路径，以/api开头
     * @return 完整的API URL
     */
    public String getApiUrl(String apiPath) {
        return String.format("%s%s", getBaseUrl(), apiPath);
    }

    // Getters
    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}