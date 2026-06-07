package com.suiyou.dto.es;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SymoblLatestResp {
    private String symbol;
    private double latestNav;
    private LocalDate latestNavDate;
    private LocalDateTime navUpdatedAt;
}
