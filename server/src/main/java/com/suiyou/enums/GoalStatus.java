package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoalStatus {

    ON_GOING("ON_GOING", "进行中"),

    COMPLETED("COMPLETED", "已完成"),

    ABANDONED("ABANDONED", "已放弃");

    private final String code;
    private final String name;

    public static GoalStatus ofCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}