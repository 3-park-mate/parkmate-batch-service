package com.parkmate.batchservice.hostsettlement.infrastructure.feignclient;

import com.parkmate.batchservice.hostsettlement.vo.response.ParkingLotHostUuidResponseVo;
import com.parkmate.batchservice.hostsettlement.vo.response.ParkingLotInfoResponseVo;
import com.parkmate.batchservice.common.response.ApiResponse;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotSalesSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "parking-service")
public interface ParkingLotInternalClient {

    @GetMapping("/internal/parkingLots/{parkingLotUuid}/host")
    ParkingLotHostUuidResponseVo getHostUuidByParkingLotUuid(@PathVariable("parkingLotUuid") String parkingLotUuid);

    @GetMapping("/internal/parkingLots/{parkingLotUuid}/info")
    ParkingLotInfoResponseVo getParkingLotInfo(@PathVariable("parkingLotUuid") String parkingLotUuid);

    @GetMapping("/internal/settlements/parking-lots/sales/summary")
    ApiResponse<List<ParkingLotSalesSummaryDto>> getParkingLotSalesSummary(
            @RequestHeader("X-Host-UUID") String hostUuid,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam(value = "week", required = false) Integer week
    );
}