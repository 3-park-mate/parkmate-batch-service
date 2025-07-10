package com.parkmate.batchservice.hostsettlement.dto.response;

import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotWeeklySalesDto {
    private String parkingLotUuid;
    private String parkingLotName;
    private List<DailySalesResponseDto> dailySalesList;
} 