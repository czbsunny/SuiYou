package com.suiyou.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InterestType {
    SIMPLE("单利"),
    COMPOUND("复利");

    private final String desc;
}