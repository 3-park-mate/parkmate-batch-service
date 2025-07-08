package com.parkmate.batchservice.hostsettlement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotSalesSummaryDto {
    private String parkingLotUuid;
    private String parkingLotName;
    private int monthlySales;
    private int weeklySales;
} 