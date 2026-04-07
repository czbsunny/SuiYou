package com.suiyou.core.es;

import com.suiyou.dto.es.FundSearchReq;
import com.suiyou.dto.es.FundSearchResp;
import com.suiyou.dto.es.FundSearchBase;
import com.suiyou.dto.es.FundChangeVO;
import com.suiyou.dto.es.SymoblLatestResp;

import java.util.List;
import java.util.Map;

public interface ElasticsearchService {

    /**
     * 检查Elasticsearch是否可用
     */
    boolean isAvailable();

    /**
     * 搜索基金
     */
    FundSearchResp searchFunds(FundSearchReq request);

    /**
     * 搜索基金
     */
    FundSearchBase searchFunds(String fundName);

    /**
     * 批量获取基金预估涨跌数据
     */
    Map<String, FundChangeVO> getFundChanges(List<String> fundCodes);

    /**
     * 批量获取基金最新净值数据
     */
    Map<String, SymoblLatestResp> getFundLatestNavs(List<String> fundCodes);

    /**
     * 批量获取股票最新净值数据
     */
    Map<String, SymoblLatestResp> getStockLatestNavs(List<String> stockCodes);
}