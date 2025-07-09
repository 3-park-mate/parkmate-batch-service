package com.parkmate.batchservice.hostsettlement.presentation;

import com.parkmate.batchservice.hostsettlement.application.HostSettlementService;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotSalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotWeeklySalesDto;
import com.parkmate.batchservice.hostsettlement.dto.response.FlexibleWeeklyStatisticsDto;
import com.parkmate.batchservice.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/internal/settlements")
@RequiredArgsConstructor
public class HostSettlementInternalController {

    private final HostSettlementService hostSettlementService;

    @GetMapping("/daily")
    public DailySalesResponseDto getDailySales(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("parkingLotUuid") String parkingLotUuid,
            @RequestParam("date") String date // format: YYYY-MM-DD
    ) {
        LocalDate localDate = LocalDate.parse(date);
        return hostSettlementService.getDailySales(hostUuid, parkingLotUuid, localDate);
    }

    @GetMapping("/monthly")
    public MonthlySalesResponseDto getMonthlySales(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("parkingLotUuid") String parkingLotUuid,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam("cycle") SettlementCycle cycle
    ) {
        return hostSettlementService.getMonthlySales(hostUuid, parkingLotUuid, year, month, cycle);
    }

    @GetMapping("/weekly")
    public List<DailySalesResponseDto> getWeeklySales(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("parkingLotUuid") String parkingLotUuid,
            @RequestParam("year") int year,
            @RequestParam("week") int week
    ) {
        return hostSettlementService.getWeeklySalesByParkingLot(hostUuid, parkingLotUuid, year, week);
    }

    @GetMapping("/weekly-range")
    public List<DailySalesResponseDto> getWeeklySalesByRange(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("parkingLotUuid") String parkingLotUuid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {
        return hostSettlementService.getWeeklySalesByParkingLotAndRange(hostUuid, parkingLotUuid, startDate, endDate);
    }

    @GetMapping("/parking-lots/sales/summary")
    public ApiResponse<List<ParkingLotSalesSummaryDto>> getParkingLotSalesSummary(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("year") int year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "weekOfMonth", required = false) Integer weekOfMonth
    ) {
        return ApiResponse.ok(
            hostSettlementService.getParkingLotSalesSummaryFlexible(
                hostUuid, year, month, weekOfMonth
            )
        );
    }

    @GetMapping("/parking-lots/sales/summary-by-range")
    public List<ParkingLotSalesSummaryDto> getParkingLotSalesSummaryByRange(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {
        return hostSettlementService.getParkingLotSalesSummaryByRange(hostUuid, startDate, endDate);
    }

    @GetMapping("/parking-lots/sales/summary-by-week")
    public ApiResponse<List<ParkingLotSalesSummaryDto>> getParkingLotSalesSummaryByWeek(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam("weekOfMonth") int weekOfMonth
    ) {
        LocalDate[] range = getWeekRange(year, month, weekOfMonth);
        if (range == null) {
            return ApiResponse.ok(java.util.Collections.emptyList());
        }
        return ApiResponse.ok(
            hostSettlementService.getParkingLotSalesSummaryByRange(
                hostUuid, range[0].toString(), range[1].toString()
            )
        );
    }

    @GetMapping("/parking-lots/weekly-statistics-flexible")
    public ApiResponse<FlexibleWeeklyStatisticsDto> getFlexibleWeeklyStatistics(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("baseDate") String baseDate,
            @RequestParam(value = "daysBefore", required = false) Integer daysBefore,
            @RequestParam(value = "daysAfter", required = false) Integer daysAfter
    ) {
        FlexibleWeeklyStatisticsDto result = hostSettlementService.getFlexibleWeeklyStatistics(hostUuid, baseDate, daysBefore, daysAfter);
        return ApiResponse.ok(result);
    }

    // 월의 n주차(1~5) 날짜 범위 계산 유틸리티
    private LocalDate[] getWeekRange(int year, int month, int weekOfMonth) {
        LocalDate start = LocalDate.of(year, month, 1);
        int startDay = (weekOfMonth - 1) * 7 + 1;
        int endDay = Math.min(weekOfMonth * 7, start.lengthOfMonth());
        if (startDay > start.lengthOfMonth()) {
            return null;
        }
        LocalDate weekStart = LocalDate.of(year, month, startDay);
        LocalDate weekEnd = LocalDate.of(year, month, endDay);
        return new LocalDate[]{weekStart, weekEnd};
    }

    @GetMapping("/internal/settlements/weekly/all")
    public ApiResponse<List<ParkingLotWeeklySalesDto>> getWeeklySalesAll(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam int year,
            @RequestParam int week) {
        return ApiResponse.ok(hostSettlementService.getWeeklySalesAll(hostUuid, year, week));
    }

    @GetMapping("/weekly/range/all")
    public ApiResponse<List<ParkingLotWeeklySalesDto>> getParkingLotsWeeklySalesByRange(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {
        return ApiResponse.ok(hostSettlementService.getParkingLotsWeeklySalesByRange(hostUuid, startDate, endDate));
    }
} 