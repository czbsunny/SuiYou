package com.suiyou.service.impl;

import com.suiyou.core.es.ElasticsearchService;
import com.suiyou.dto.es.FundSearchReq;
import com.suiyou.dto.es.FundSearchResp;
import com.suiyou.dto.es.FundSearchBase;
import com.suiyou.service.FundSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 基金搜索服务实现类
 */
@Service
public class FundSearchServiceImpl implements FundSearchService {

    private static final Logger logger = LoggerFactory.getLogger(FundSearchServiceImpl.class);
    
    private final ElasticsearchService elasticsearchService;
    
    @Autowired
    public FundSearchServiceImpl(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }
    
    @Override
    public FundSearchResp searchFunds(String keyword, String fundType, int page, int pageSize) {
        logger.info("搜索基金: keyword={}, fundType={}, page={}, pageSize={}", 
                keyword, fundType, page, pageSize);
        
        // 首先尝试使用Elasticsearch搜索
        if (elasticsearchService.isAvailable()) {
            FundSearchReq request = new FundSearchReq();
            request.setKeyword(keyword);
            request.setFundType(fundType);
            request.setPage(page);
            request.setPageSize(pageSize);
            
            FundSearchResp response = elasticsearchService.searchFunds(request);
            if (response.isSuccess()) {
                logger.info("Elasticsearch搜索成功，找到 {} 条结果", response.getTotal());
                return response;
            } else {
                logger.warn("Elasticsearch搜索失败，回退到数据库搜索");
            }
        } else {
            logger.info("Elasticsearch不可用，使用数据库搜索");
        }
        
        // Elasticsearch不可用或搜索失败，回退到数据库搜索
        return null;
    }
    
    @Override
    public FundSearchBase searchFunds(String fundName) {
        FundSearchBase response = null;
        if (elasticsearchService.isAvailable()) {
            response = elasticsearchService.searchFunds(fundName);
            if (response != null) {
                logger.info("Elasticsearch搜索成功，找到 {} 条结果", response.getScore());
            } else {
                logger.warn("Elasticsearch搜索失败，回退到数据库搜索");
            }
        } else {
            logger.info("Elasticsearch不可用，使用数据库搜索");
        }

        return response;
    }
}