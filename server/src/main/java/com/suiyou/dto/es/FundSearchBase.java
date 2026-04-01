package com.suiyou.dto.es;

import lombok.Data;

@Data
public class FundSearchBase {
    private String fundCode;
    private String name;
    private float score;
}