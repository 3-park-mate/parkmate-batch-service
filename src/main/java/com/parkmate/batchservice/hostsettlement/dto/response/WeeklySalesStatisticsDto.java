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
    private int totalWeeklySales;        // 주간 총 매출
    private int averageDailySales;      // 일평균 매출
    private int maxDailySales;          // 최고 일매출
    private int minDailySales;          // 최저 일매출
    private int totalDays;              // 총 일수
    private int salesDays;              // 매출 발생 일수
    private double salesRate;            // 매출 발생률 (salesDays / totalDays)
    private String weekRange;            // 주차 범위 (예: "2024-01-01 ~ 2024-01-07")
} 