package com.parkmate.batchservice.hostsettlement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlexibleWeeklyStatisticsDto {
    private int totalWeeklySales; // 주간 총 매출 (호스트 전체)
}