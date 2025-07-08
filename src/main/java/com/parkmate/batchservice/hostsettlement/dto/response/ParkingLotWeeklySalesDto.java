package com.parkmate.batchservice.hostsettlement.dto.response;

import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import java.util.List;

public class ParkingLotWeeklySalesDto {
    private String parkingLotUuid;
    private String parkingLotName;
    private List<DailySalesResponseDto> dailySalesList;

    public ParkingLotWeeklySalesDto(String parkingLotUuid, String parkingLotName, List<DailySalesResponseDto> dailySalesList) {
        this.parkingLotUuid = parkingLotUuid;
        this.parkingLotName = parkingLotName;
        this.dailySalesList = dailySalesList;
    }

    public String getParkingLotUuid() {
        return parkingLotUuid;
    }

    public String getParkingLotName() {
        return parkingLotName;
    }

    public List<DailySalesResponseDto> getDailySalesList() {
        return dailySalesList;
    }
} 