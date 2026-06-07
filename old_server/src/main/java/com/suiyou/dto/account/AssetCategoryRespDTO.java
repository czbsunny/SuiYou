package com.suiyou.dto.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 资产分类视图对象 (树形节点)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCategoryRespDTO {
    private Long id;

    private String name;

    private String categoryCode;

    private String iconUrl;
    
    private String color;
    
    private Integer sortOrder;

    @Builder.Default
    private List<AssetCategoryRespDTO> children = new ArrayList<>();
}