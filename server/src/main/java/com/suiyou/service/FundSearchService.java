package com.suiyou.service;

import com.suiyou.dto.es.FundSearchResp;
import com.suiyou.dto.es.FundSearchBase;

/**
 * 基金搜索服务接口
 * 提供基金搜索相关功能
 */
public interface FundSearchService {
    
    /**
     * 搜索基金
     * @param keyword 搜索关键词
     * @param fundType 基金类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    FundSearchResp searchFunds(String keyword, String fundType, int page, int pageSize);

    /**
     * 搜索基金
     * @param fundName 基金名称
     * @return 搜索结果
     */
    FundSearchBase searchFunds(String fundName);
}