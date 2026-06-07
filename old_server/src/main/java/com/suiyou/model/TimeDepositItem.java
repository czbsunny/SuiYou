package com.suiyou.model;

import com.suiyou.model.enums.InterestType;
import com.suiyou.model.enums.PayoutType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "time_deposit_item")
@Data
public class TimeDepositItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal principal;

    @Column(name = "annual_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal annualRate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false, length = 20)
    private InterestType interestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_type", nullable = false, length = 20)
    private PayoutType payoutType;

    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal = false;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Integer status = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotalExpectedInterest() {
        if (principal == null || annualRate == null || startDate == null || maturityDate == null) {
            return BigDecimal.ZERO;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, maturityDate);
        if (totalDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);

        if (interestType == InterestType.SIMPLE) {
            return principal.multiply(dailyRate).multiply(new BigDecimal(totalDays));
        } else {
            BigDecimal growthFactor = dailyRate.add(BigDecimal.ONE).pow((int) totalDays);
            return principal.multiply(growthFactor).subtract(principal);
        }
    }

    public BigDecimal calculateAccruedInterest() {
        return calculateAccruedInterest(LocalDate.now());
    }

    public BigDecimal calculateAccruedInterest(LocalDate asOfDate) {
        if (principal == null || annualRate == null || startDate == null) {
            return BigDecimal.ZERO;
        }

        LocalDate endDate = maturityDate != null && asOfDate.isAfter(maturityDate) ? maturityDate : asOfDate;
        
        if (endDate.isBefore(startDate)) {
            return BigDecimal.ZERO;
        }

        long elapsedDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (elapsedDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);

        if (interestType == InterestType.SIMPLE) {
            return principal.multiply(dailyRate).multiply(new BigDecimal(elapsedDays));
        } else {
            BigDecimal growthFactor = dailyRate.add(BigDecimal.ONE).pow((int) elapsedDays);
            return principal.multiply(growthFactor).subtract(principal);
        }
    }

    public long calculateDaysRemaining() {
        return calculateDaysRemaining(LocalDate.now());
    }

    public long calculateDaysRemaining(LocalDate asOfDate) {
        if (maturityDate == null) {
            return 0;
        }

        long daysRemaining = ChronoUnit.DAYS.between(asOfDate, maturityDate);
        return Math.max(0, daysRemaining);
    }

    public BigDecimal calculateProgressPercentage() {
        return calculateProgressPercentage(LocalDate.now());
    }

    public BigDecimal calculateProgressPercentage(LocalDate asOfDate) {
        if (startDate == null || maturityDate == null) {
            return BigDecimal.ZERO;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, maturityDate);
        if (totalDays <= 0) {
            return BigDecimal.ZERO;
        }

        LocalDate endDate = asOfDate.isAfter(maturityDate) ? maturityDate : asOfDate;
        long elapsedDays = ChronoUnit.DAYS.between(startDate, endDate);
        
        if (elapsedDays <= 0) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(elapsedDays).divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public BigDecimal calculateMaturityAmount() {
        return principal.add(calculateTotalExpectedInterest());
    }

    public BigDecimal calculateCurrentValue() {
        return principal.add(calculateAccruedInterest());
    }
}