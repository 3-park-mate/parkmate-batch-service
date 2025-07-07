package com.parkmate.batchservice.hostsettlement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesSummaryDto(
    String hostUuid,
    String parkingLotUuid,
    LocalDate settlementDate,
    BigDecimal totalAmount
) {} 