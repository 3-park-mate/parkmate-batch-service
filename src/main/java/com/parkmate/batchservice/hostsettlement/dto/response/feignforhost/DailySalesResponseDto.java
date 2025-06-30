package com.parkmate.batchservice.hostsettlement.dto.response.feignforhost;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesResponseDto {
    private LocalDate date;
    private BigDecimal totalSalesAmount;
}
