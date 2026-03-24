package com.suiyou.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
/**
 * 分类响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRespDTO {
    private Long id;

    private String name;

    private String categoryCode;

    private String iconUrl;
    
    private String color;
    
    private Integer sortOrder;

    @Builder.Default
    private List<CategoryRespDTO> children = new ArrayList<>();
}
