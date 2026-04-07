package com.suiyou.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suiyou.config.DataServerConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class DataServerClient {
    @Autowired
    private DataServerConfig dataServerConfig;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DataServerClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Double> getFundLatestNavs(List<String> fundCodes) {
        try {
            String url = dataServerConfig.getApiUrl("/api/fund/nav");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("fund_codes", fundCodes);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                Map<String, Double> result = new HashMap<>();

                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String fundCode = entry.getKey();
                    double nav = entry.getValue().asDouble();
                    result.put(fundCode, nav);
                }

                return result;
            }

            return Collections.emptyMap();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public Map<String, Double> getStockLatestNavs(List<String> stockCodes) {
        try {
            String url = dataServerConfig.getApiUrl("/api/stock/nav");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("stock_codes", stockCodes);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                Map<String, Double> result = new HashMap<>();

                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String stockCode = entry.getKey();
                    double nav = entry.getValue().asDouble();
                    result.put(stockCode, nav);
                }

                return result;
            }

            return Collections.emptyMap();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
