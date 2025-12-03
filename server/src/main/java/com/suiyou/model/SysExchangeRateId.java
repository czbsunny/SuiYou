package com.suiyou.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class SysExchangeRateId implements Serializable {
    private LocalDate date;
    private String sourceCurrency;
    private String targetCurrency;
}
