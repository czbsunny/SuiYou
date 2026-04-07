package com.suiyou.core.es.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhrasePrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.suiyou.config.ElasticsearchConfig;
import com.suiyou.core.es.ElasticsearchService;
import com.suiyou.dto.es.FundDocument;
import com.suiyou.dto.es.FundSearchBase;
import com.suiyou.dto.es.FundSearchReq;
import com.suiyou.dto.es.FundSearchResp;
import com.suiyou.dto.es.FundChangeVO;
import com.suiyou.dto.es.FundLatestResp;
import com.suiyou.dto.es.StockLatestResp;
import com.suiyou.dto.es.SymoblLatestResp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Autowired
    private ElasticsearchConfig elasticsearchConfig;

    @Autowired
    private ElasticsearchClient esClient;

    @Override
    public boolean isAvailable() {
        return elasticsearchConfig.isEnableElasticsearch() && esClient != null;
    }

    @SuppressWarnings("null")
    @Override
    public FundSearchResp searchFunds(FundSearchReq request) {
        FundSearchResp response = new FundSearchResp();
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());

        if (!isAvailable()) {
            response.setSuccess(false);
            response.setTotal(0);
            response.setFunds(new ArrayList<>());
            return response;
        }

        try {
            List<Query> shouldQueries = new ArrayList<>();

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = request.getKeyword().trim();
                boolean isNumeric = keyword.matches("\\d+");

                // fundCode 数字模糊匹配（wildcard 支持前中后缀）
                if (isNumeric) {
                    shouldQueries.add(WildcardQuery.of(w -> w
                        .field("fundCode")
                        .value("*" + keyword + "*")
                        .boost(10.0f)
                    )._toQuery());
                }

                // 中文 name / fullName 分词 + fuzziness 容错
                shouldQueries.add(MatchQuery.of(m -> m
                        .field("name")
                        .query(keyword)
                        .fuzziness("AUTO")
                        .boost(5.0f)
                )._toQuery());

                shouldQueries.add(MatchQuery.of(m -> m
                        .field("fullName")
                        .query(keyword)
                        .fuzziness("AUTO")
                        .boost(4.5f)
                )._toQuery());

                // 拼音 name / fullName 搜索
                shouldQueries.add(MatchQuery.of(m -> m
                        .field("name.pinyin")
                        .query(keyword.toLowerCase())
                        .fuzziness("AUTO")
                        .boost(3.5f)
                )._toQuery());

                shouldQueries.add(MatchQuery.of(m -> m
                        .field("fullName.pinyin")
                        .query(keyword.toLowerCase())
                        .fuzziness("AUTO")
                        .boost(3.0f)
                )._toQuery());

                // 前缀增强（中文）
                shouldQueries.add(MatchPhrasePrefixQuery.of(m -> m
                        .field("name")
                        .query(keyword)
                        .maxExpansions(50)
                        .boost(2.5f)
                )._toQuery());

                shouldQueries.add(MatchPhrasePrefixQuery.of(m -> m
                        .field("fullName")
                        .query(keyword)
                        .maxExpansions(50)
                        .boost(2.0f)
                )._toQuery());

                // 拼音前缀增强
                shouldQueries.add(MatchPhrasePrefixQuery.of(m -> m
                        .field("name.pinyin")
                        .query(keyword.toLowerCase())
                        .maxExpansions(50)
                        .boost(1.5f)
                )._toQuery());

                shouldQueries.add(MatchPhrasePrefixQuery.of(m -> m
                        .field("fullName.pinyin")
                        .query(keyword.toLowerCase())
                        .maxExpansions(50)
                        .boost(1.0f)
                )._toQuery());
            }

            // 构建搜索请求
            SearchRequest searchRequest;
            if (shouldQueries.isEmpty() && (request.getFundType() == null || request.getFundType().trim().isEmpty())) {
                searchRequest = SearchRequest.of(s -> s
                        .index(elasticsearchConfig.getFundIndex())
                        .query(MatchAllQuery.of(m -> m)._toQuery())
                        .from((request.getPage() - 1) * request.getPageSize())
                        .size(request.getPageSize())
                        .sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)))
                );
            } else {
                searchRequest = SearchRequest.of(s -> s
                        .index(elasticsearchConfig.getFundIndex())
                        .query(BoolQuery.of(b -> {
                            if (!shouldQueries.isEmpty()) {
                                b.should(shouldQueries);
                                b.minimumShouldMatch("1");
                            }
                            if (request.getFundType() != null && !request.getFundType().trim().isEmpty()) {
                                b.filter(TermQuery.of(t -> t
                                        .field("fundType")
                                        .value(request.getFundType().trim())
                                )._toQuery());
                            }
                            return b;
                        })._toQuery())
                        .from((request.getPage() - 1) * request.getPageSize())
                        .size(request.getPageSize())
                        .sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)))
                );
            }

            SearchResponse<FundDocument> searchResponse = esClient.search(searchRequest, FundDocument.class);

            // 处理结果
            response.setSuccess(true);
            response.setTotal(searchResponse != null && searchResponse.hits() != null && searchResponse.hits().total() != null ?
                    searchResponse.hits().total().value() : 0L);

            List<FundSearchResp.FundSearchResult> results = new ArrayList<>();
            for (Hit<FundDocument> hit : searchResponse.hits().hits()) {
                FundDocument fund = hit.source();
                if (fund != null) {
                    FundSearchResp.FundSearchResult result = new FundSearchResp.FundSearchResult();
                    result.setFundCode(fund.getFundCode());
                    result.setName(fund.getName());
                    result.setFundType(fund.getFundType());
                    result.setCompany(fund.getCompany());
                    result.setManager(fund.getManager());
                    result.setScore(hit.score() != null ? hit.score().floatValue() : 0.0f);
                    // 净值相关字段
                    result.setLatestNav(fund.getLatestNav());
                    result.setLatestNavDate(fund.getLatestNavDate());
                    result.setDailyGrowth(fund.getDailyGrowth());
                    result.setNavUpdatedAt(fund.getNavUpdatedAt());
                    results.add(result);
                }
            }
            response.setFunds(results);
            return response;

        } catch (Exception e) {
            log.error("搜索基金失败: {}", e.getMessage());
            response.setSuccess(false);
            response.setTotal(0);
            response.setFunds(new ArrayList<>());
            return response;
        }
    }

    @Override
    public FundSearchBase searchFunds(String fundName) {
        try {
            Query query = Query.of(q -> q
                .bool(b -> b
                    .should(
                        sh -> sh.multiMatch(m -> m
                            .fields("name", "fullName")
                            .query(fundName)
                            .fuzziness("AUTO")
                        )
                    )
                )
            );

            SearchRequest request = SearchRequest.of(s -> s
                .index(elasticsearchConfig.getFundIndex())
                .query(query)
                .from(0)
                .size(3)
                .source(src -> src
                    .filter(f -> f.includes("name", "fundCode"))
                )
            );

            SearchResponse<FundSearchBase> searchResponse = esClient.search(request, FundSearchBase.class);

            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.error("搜索基金时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, FundChangeVO> getFundChanges(List<String> fundCodes) {
        Map<String, FundChangeVO> result = new HashMap<>();

        if (!isAvailable() || fundCodes == null || fundCodes.isEmpty()) {
            return result;
        }

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index("fund_change_latest")
                    .query(q -> q
                            .ids(i -> i.values(fundCodes))
                    )
                    .size(fundCodes.size())
            );

            SearchResponse<FundChangeVO> response =
                    esClient.search(request, FundChangeVO.class);

            for (Hit<FundChangeVO> hit : response.hits().hits()) {
                FundChangeVO vo = hit.source();
                if (vo != null) {
                    // 如果ES里没有fund_id字段，用_id兜底
                    if (vo.getFundId() == null) {
                        vo.setFundId(hit.id());
                    }
                    result.put(vo.getFundId(), vo);
                }
            }

            log.debug("ES获取基金数据 {} 条", result.size());

        } catch (Exception e) {
            log.error("ES查询失败", e);
        }

        return result;
    }

    @Override
    public Map<String, SymoblLatestResp> getFundLatestNavs(List<String> fundCodes) {
        Map<String, SymoblLatestResp> result = new HashMap<>();

        if (!isAvailable() || fundCodes == null || fundCodes.isEmpty()) {
            return result;
        }

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(elasticsearchConfig.getFundIndex())
                    .query(q -> q
                            .ids(i -> i.values(fundCodes))
                    )
                    .size(fundCodes.size())
            );

            SearchResponse<FundLatestResp> response =
                    esClient.search(request, FundLatestResp.class);

            for (Hit<FundLatestResp> hit : response.hits().hits()) {
                FundLatestResp resp = hit.source();
                if (resp != null) {
                    result.put(resp.getFundCode(), resp.toSymoblLatestResp());
                }
            }

            log.debug("ES获取基金数据 {} 条", result.size());

        } catch (Exception e) {
            log.error("ES查询失败", e);
        }

        return result;
    }

    @Override
    public Map<String, SymoblLatestResp> getStockLatestNavs(List<String> stockCodes) {
        Map<String, SymoblLatestResp> result = new HashMap<>();

        if (!isAvailable() || stockCodes == null || stockCodes.isEmpty()) {
            return result;
        }

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index("stocks")
                    .query(q -> q
                            .ids(i -> i.values(stockCodes))
                    )
                    .size(stockCodes.size())
            );

            SearchResponse<StockLatestResp> response =
                    esClient.search(request, StockLatestResp.class);

            for (Hit<StockLatestResp> hit : response.hits().hits()) {
                StockLatestResp resp = hit.source();
                if (resp != null) {
                    result.put(resp.getStockCode(), resp.toSymoblLatestResp());
                }
            }

            log.debug("ES获取股票数据 {} 条", result.size());

        } catch (Exception e) {
            log.error("ES查询失败", e);
        }

        return result;
    }
}