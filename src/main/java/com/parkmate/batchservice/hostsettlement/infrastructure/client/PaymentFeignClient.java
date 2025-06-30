package com.parkmate.batchservice.hostsettlement.infrastructure.client;

import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforpayment.SettlementPaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "payment-service")
public interface PaymentFeignClient {

    @GetMapping("/internal/settlements")
    List<SettlementPaymentResponseDto> getSettlementPayments(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("parkingLotUuid") String parkingLotUuid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );

    @GetMapping("/internal/settlements/pairs/by-date")
    List<HostParkingLotDto> getHostParkingLotPairsByDate(
            @RequestParam("date") String targetDate
    );

    @GetMapping("/internal/settlements/pairs/by-range")
    List<HostParkingLotDto> getHostParkingLotPairsBetween(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );
}