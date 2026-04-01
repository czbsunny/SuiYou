package com.suiyou.dto.es;

import lombok.Data;

@Data
public class FundSearchReq {
    private String keyword;
    private String fundType;
    private int page = 1;
    private int pageSize = 20;
}