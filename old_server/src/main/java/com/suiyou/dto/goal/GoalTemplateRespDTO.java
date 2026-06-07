package com.suiyou.dto.goal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 目标模板视图对象
 * 用于前端渲染具体的“创建目标”表单和向导逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) //以此忽略 null 字段，减少传输体积
public class GoalTemplateRespDTO {

    /**
     * 模板唯一标识 (前端传参用)
     * 对应 templateCode
     */
    private String code;

    /**
     * 所属分类编码
     */
    private String categoryCode;

    /**
     * 模板名称 (e.g. "攒首付", "存旅游基金")
     */
    private String name;

    /**
     * 图标链接
     */
    private String iconUrl;

    /**
     * 默认金额 (前端输入框的 placeholder 或 初始值)
     */
    private BigDecimal defaultAmount;

    /**
     * 默认周期天数
     */
    private Integer defaultPeriodDays;

    /**
     * 输入模式
     * SIMPLE: 简单模式 (只填金额)
     * WIZARD: 向导模式 (按步骤填)
     */
    private String inputMode;

    /**
     * 向导配置 (核心配置)
     * 对应 DB 中的 wizardConfig (String)
     * 在 VO 中转为 Object，这样返回给前端就是标准的嵌套 JSON 对象
     */
    private Object wizardConfig;

    /**
     * 里程碑策略标识
     */
    private String milestoneStrategy;

    /**
     * 里程碑具体配置
     * 同样转为 Object
     */
    private Object milestoneConfig;

    /**
     * 是否热门推荐 (转为 Boolean 方便前端 v-if)
     */
    private Boolean isHot;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
