package com.suiyou.dto.es;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundChangeVO {
    @JsonProperty("fund_id")
    private String fundId;
    private Double change;
    private String timestamp;
}