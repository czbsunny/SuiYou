package com.suiyou.repository;

import com.suiyou.model.SysExchangeRate;
import com.suiyou.model.SysExchangeRateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SysExchangeRateRepository extends JpaRepository<SysExchangeRate, SysExchangeRateId> {
    Optional<SysExchangeRate> findByDateAndSourceCurrencyAndTargetCurrency(
            LocalDate date, String sourceCurrency, String targetCurrency);
    
    List<SysExchangeRate> findBySourceCurrencyAndTargetCurrency(String sourceCurrency, String targetCurrency);
    
    List<SysExchangeRate> findByDateAndSourceCurrency(LocalDate date, String sourceCurrency);
    
    List<SysExchangeRate> findByDate(LocalDate date);
}
