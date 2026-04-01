package com.suiyou.core.es;

import com.suiyou.dto.es.FundDocument;
import com.suiyou.dto.es.FundSearchReq;
import com.suiyou.dto.es.FundSearchResp;
import com.suiyou.dto.es.FundSearchBase;
import com.suiyou.dto.es.FundChangeVO;

import java.util.List;
import java.util.Map;

public interface ElasticsearchService {

    /**
     * 检查Elasticsearch是否可用
     */
    boolean isAvailable();

    /**
     * 创建基金索引
     */
    boolean createIndex();

    /**
     * 批量索引基金数据
     */
    Map<String, Object> bulkIndexFunds(List<FundDocument> funds);

    /**
     * 搜索基金
     */
    FundSearchResp searchFunds(FundSearchReq request);

    /**
     * 搜索基金
     */
    FundSearchBase searchFunds(String fundName);

    /**
     * 索引单条基金数据
     */
    boolean indexSingleFund(FundDocument fund);

    /**
     * 删除基金索引
     */
    boolean deleteFund(String fundCode);

    /**
     * 清空索引数据
     */
    boolean clearIndex();

    /**
     * 批量获取基金预估涨跌数据
     */
    Map<String, FundChangeVO> getFundChanges(List<String> fundCodes);
}