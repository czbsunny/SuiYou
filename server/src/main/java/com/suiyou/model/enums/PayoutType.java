package com.suiyou.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayoutType {
    MATURITY("到期还本付息"),
    PERIODIC("定期付息");

    private final String desc;
}