package com.parkmate.batchservice.hostsettlement.presentation;

import com.parkmate.batchservice.hostsettlement.application.HostSettlementService;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

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
}
