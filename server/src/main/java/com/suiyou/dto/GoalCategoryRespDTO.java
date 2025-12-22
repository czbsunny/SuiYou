package com.suiyou.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCategoryRespDTO {
    /**
     * 业务唯一标识 (前端用这个做路由或逻辑判断)
     * 对应 Entity 的 categoryCode
     */
    private String code;

    /**
     * 显示名称 (e.g., "买房置业")
     */
    private String name;

    /**
     * 标语/描述 (e.g., "为理想的家存下第一笔金")
     */
    private String slogan;

    /**
     * 图标链接
     */
    private String iconUrl;

    /**
     * 背景色 (e.g., "#FF5733")
     */
    private String bgColor;
    
    // 如果前端排序完全依赖后端返回的数组顺序，这里甚至不需要 sortOrder 字段
}