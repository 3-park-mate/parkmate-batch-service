package com.parkmate.batchservice.hostsettlement.presentation;

import com.parkmate.batchservice.hostsettlement.application.HostSettlementService;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotSalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotWeeklySalesDto;
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
    public List<ParkingLotSalesSummaryDto> getParkingLotSalesSummary(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam(value = "week", required = false) Integer week
    ) {
        try {
            List<ParkingLotSalesSummaryDto> result = hostSettlementService.getParkingLotSalesSummary(hostUuid, year, month, week);
            return result != null ? result : java.util.Collections.emptyList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
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