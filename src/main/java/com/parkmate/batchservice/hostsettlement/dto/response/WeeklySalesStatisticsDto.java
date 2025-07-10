package com.parkmate.batchservice.hostsettlement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySalesStatisticsDto {
    private String parkingLotUuid;
    private String parkingLotName;
    private int totalWeeklySales;
    private int averageDailySales;
    private int maxDailySales;
    private int minDailySales;
    private int totalDays;
    private int salesDays;
    private double salesRate;
    private String weekRange;
} 