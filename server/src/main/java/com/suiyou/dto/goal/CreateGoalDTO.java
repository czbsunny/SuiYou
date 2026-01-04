package com.suiyou.dto.goal;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalDTO {
    @NotBlank(message = "目标名称不能为空")
    @Size(max = 128, message = "目标名称长度不能超过128个字符")
    private String name;

    @NotNull(message = "目标金额不能为空")
    @DecimalMin(value = "0.01", message = "目标金额必须大于0")
    private BigDecimal targetAmount;

    @NotNull(message = "截止日期不能为空")
    private LocalDate deadline;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Size(max = 20, message = "可见范围长度不能超过20个字符")
    private String visibleScope = "PRIVATE";

    @Size(max = 255, message = "图标URL长度不能超过255个字符")
    private String iconUrl; // 存储图标路径或 Emoji 字符

    @Size(max = 255, message = "背景URL长度不能超过255个字符")
    private String bgUrl; // 主线目标的背景大图

    @NotNull(message = "是否为主线目标不能为空")
    private Boolean isPrimary = false; // 是否为主线目标
}