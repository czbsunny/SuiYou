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

    /**
     * 分类ID (前端无论是做 key 还是传参都需要)
     */
    private Long id;

    /**
     * 分类名称 (e.g., "银行账户")
     */
    private String name;

    /**
     * 业务编码 (e.g., "bank_account", 用于前端逻辑判断)
     */
    private String code;

    /**
     * 图标 URL 或 CSS 类名
     */
    private String icon;
    
    /**
     * 绑定的表单 Key (前端根据这个决定弹什么样式的框)
     * e.g., "form_credit_card"
     */
    private String formKey;

    /**
     * 排序号 (可选，前端可能需要)
     */
    private Integer sortOrder;

    /**
     * 子节点列表
     * 初始化为空列表，防止前端拿到 null 报错
     */
    @Builder.Default
    private List<AssetCategoryRespDTO> children = new ArrayList<>();
}