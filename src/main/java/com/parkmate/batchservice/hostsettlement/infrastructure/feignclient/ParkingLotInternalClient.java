package com.parkmate.batchservice.hostsettlement.infrastructure.feignclient;

import com.parkmate.batchservice.hostsettlement.vo.response.ParkingLotHostUuidResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "parking-service")
public interface ParkingLotInternalClient {

    @GetMapping("/internal/parkingLots/{parkingLotUuid}/host")
    ParkingLotHostUuidResponseVo getHostUuidByParkingLotUuid(@PathVariable("parkingLotUuid") String parkingLotUuid);
}