package com.parkmate.batchservice.hostsettlement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostParkingLotDto {

    private String hostUuid;
    private String parkingLotUuid;

}
