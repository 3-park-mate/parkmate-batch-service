package com.parkmate.batchservice.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.parkmate.batchservice.hostsettlement.domain.ReservationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationEvent {

    private String reservationCode;
    private String parkingLotUuid;
    @Setter
    private String hostUuid;
    private long amount;
    private ReservationStatus status;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private LocalDateTime timestamp;
}
