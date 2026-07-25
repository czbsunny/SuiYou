package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoalPriority {

    CRITICAL("CRITICAL", "核心"),

    IMPORTANT("IMPORTANT", "重要"),

    OPTIONAL("OPTIONAL", "一般");

    private final String code;
    private final String name;

    public static GoalPriority ofCode(String code) {
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