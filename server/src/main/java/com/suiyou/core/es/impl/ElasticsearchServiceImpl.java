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
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.suiyou.config.ElasticsearchConfig;
import com.suiyou.core.es.ElasticsearchService;
import com.suiyou.dto.es.FundDocument;
import com.suiyou.dto.es.FundSearchBase;
import com.suiyou.dto.es.FundSearchReq;
import com.suiyou.dto.es.FundSearchResp;
import com.suiyou.dto.es.FundChangeVO;
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

    @Override
    public boolean createIndex() {
        if (!isAvailable()) {
            return false;
        }

        String indexName = elasticsearchConfig.getFundIndex();

        try {
            // 1. 检查索引是否已存在
            boolean exists = esClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();
            if (exists) {
                log.info("索引 {} 已存在", indexName);
                return true;
            }

            log.info("开始创建索引 {}", indexName);

            // 2. 创建索引请求
            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
                .index(indexName)
                .settings(s -> s
                    .numberOfShards("1")
                    .numberOfReplicas("0")
                    .analysis(a -> a
                        // pinyin analyzer for name/full_name
                        .analyzer("pinyin_analyzer", an -> an
                            .custom(ca -> ca
                                .tokenizer("ik_max_word")
                                .filter("lowercase")
                            )
                        )
                    )
                )
                .mappings(m -> m
                    .properties("fundCode", p -> p.keyword(k -> k))
                    .properties("name", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                        .fields("pinyin", f -> f.text(tt -> tt.analyzer("pinyin_analyzer")))
                        .fields("raw", f -> f.keyword(k -> k))
                    ))
                    .properties("fullName", p -> p.text(t -> t
                        .analyzer("ik_max_word")
                        .fields("pinyin", f -> f.text(tt -> tt.analyzer("pinyin_analyzer")))
                        .fields("raw", f -> f.keyword(k -> k))
                    ))
                    .properties("fundType", p -> p.keyword(k -> k))
                    .properties("company", p -> p.keyword(k -> k))
                    .properties("manager", p -> p.keyword(k -> k))
                    .properties("establishDate", p -> p.date(d -> d))
                    .properties("riskLevel", p -> p.keyword(k -> k))
                    .properties("rating", p -> p.keyword(k -> k))
                )
            );

            // 3. 执行创建
            esClient.indices().create(createIndexRequest);

            log.info("成功创建索引 {}", indexName);
            return true;

        } catch (Exception e) {
            log.error("创建索引 {} 失败: {}", indexName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> bulkIndexFunds(List<FundDocument> funds) {
        if (!isAvailable() || funds == null || funds.isEmpty()) {
            return Map.of("success", false, "total", 0, "success_count", 0);
        }

        try {
            List<BulkOperation> operations = new ArrayList<>();
            for (FundDocument fund : funds) {
                IndexOperation<FundDocument> indexOp = IndexOperation.of(i -> i
                        .index(elasticsearchConfig.getFundIndex())
                        .id(fund.getFundCode())
                        .document(fund)
                );
                operations.add(BulkOperation.of(o -> o.index(indexOp)));
            }

            BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(operations));

            long startTime = System.currentTimeMillis();
            BulkResponse bulkResponse = esClient.bulk(bulkRequest);
            long endTime = System.currentTimeMillis();

            int successCount = (int) (operations.size() - bulkResponse.items().stream()
                    .filter(item -> item.error() != null)
                    .count());
            
            log.info("批量索引 {} 条基金数据，成功 {} 条，耗时 {}秒", 
                    funds.size(), successCount, (endTime - startTime) / 1000.0);

            return Map.of(
                    "success", !bulkResponse.errors(),
                    "total", funds.size(),
                    "success_count", successCount
            );
        } catch (Exception e) {
            log.error("批量索引基金数据失败: {}", e.getMessage());
            return Map.of("success", false, "total", funds.size(), "success_count", 0);
        }
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
    public boolean indexSingleFund(FundDocument fund) {
        if (!isAvailable()) {
            return false;
        }

        try {
            IndexRequest<FundDocument> request = IndexRequest.of(i -> i
                    .index(elasticsearchConfig.getFundIndex())
                    .id(fund.getFundCode())
                    .document(fund)
            );

            esClient.index(request);
            return true;
        } catch (Exception e) {
            log.error("索引基金 {} 失败: {}", fund.getFundCode(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFund(String fundCode) {
        if (!isAvailable()) {
            return false;
        }

        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(elasticsearchConfig.getFundIndex())
                    .id(fundCode)
            );

            esClient.delete(request);
            return true;
        } catch (Exception e) {
            log.error("删除基金 {} 索引失败: {}", fundCode, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean clearIndex() {
        if (!isAvailable()) {
            return false;
        }

        try {
            // 使用删除所有文档的方式清空索引
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(elasticsearchConfig.getFundIndex())
                    .query(MatchAllQuery.of(m -> m)._toQuery())
                    .size(10000)
            );

            SearchResponse<FundDocument> searchResponse = esClient.search(searchRequest, FundDocument.class);
            List<BulkOperation> deleteOperations = new ArrayList<>();

            for (Hit<FundDocument> hit : searchResponse.hits().hits()) {
                deleteOperations.add(BulkOperation.of(o -> o.delete(d -> d
                        .index(elasticsearchConfig.getFundIndex())
                        .id(hit.id())
                )));
            }

            if (!deleteOperations.isEmpty()) {
                BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(deleteOperations));
                esClient.bulk(bulkRequest);
            }

            log.info("清空索引 {} 成功", elasticsearchConfig.getFundIndex());
            return true;
        } catch (Exception e) {
            log.error("清空索引 {} 失败: {}", elasticsearchConfig.getFundIndex(), e.getMessage());
            return false;
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
}