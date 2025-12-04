package com.suiyou.dto.account;

import lombok.Data;

import java.util.List;

@Data
public class CategoryInitDTO {
    private String code;
    private String name;
    private String groupType;
    private String iconUrl;
    private Integer sortOrder;
    private String color;
    private List<CategoryInitDTO> children;
}
