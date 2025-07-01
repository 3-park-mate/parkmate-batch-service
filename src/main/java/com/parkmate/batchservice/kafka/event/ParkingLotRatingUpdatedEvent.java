package com.parkmate.batchservice.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParkingLotRatingUpdatedEvent {

    private String parkingLotUuid;
    private double averageRating;

    @Builder
    private ParkingLotRatingUpdatedEvent(String parkingLotUuid, double averageRating) {
        this.parkingLotUuid = parkingLotUuid;
        this.averageRating = averageRating;
    }
}