package com.suiyou.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sys_exchange_rate")
@Data
@IdClass(SysExchangeRateId.class)
public class SysExchangeRate {
    @Id
    private LocalDate date;
    
    @Id
    @Column(name = "source_currency", nullable = false)
    private String sourceCurrency;
    
    @Id
    @Column(name = "target_currency", nullable = false)
    private String targetCurrency;
    
    @Column(nullable = false, columnDefinition = "decimal(19,6)")
    private BigDecimal rate;
}
