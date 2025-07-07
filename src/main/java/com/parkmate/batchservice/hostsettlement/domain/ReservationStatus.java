package com.parkmate.batchservice.hostsettlement.domain;

import lombok.Getter;

@Getter
public enum ReservationStatus {

    WAITING("예약 대기"),
    CONFIRMED("예약 확정"),
    CANCELLED("예약 취소"),
    EXPIRED("예약 만료"),
    IN_USE("사용중"),
    COMPLETED("사용 완료")
    ;

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }
}