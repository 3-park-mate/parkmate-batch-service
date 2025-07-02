package com.parkmate.batchservice.hostsettlement.dto.response.feignforhost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesResponseDto {
    private String yearMonth; // ex: "2025-06"
    private BigDecimal totalSalesAmount;
}
