package com.suiyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoalTerm {

    SHORT("SHORT", "短期"),

    MEDIUM("MEDIUM", "中期"),

    LONG("LONG", "长期");

    private final String code;
    private final String name;

    public static GoalTerm ofCode(String code) {
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