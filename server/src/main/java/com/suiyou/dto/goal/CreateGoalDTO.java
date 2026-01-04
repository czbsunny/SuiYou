package com.suiyou.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalDTO {

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

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getVisibleScope() {
        return visibleScope;
    }

    public void setVisibleScope(String visibleScope) {
        this.visibleScope = visibleScope;
    }
}