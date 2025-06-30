package com.parkmate.batchservice.hostsettlement.domain;

import lombok.Getter;

@Getter
public enum SettlementCycle {
    DAILY(1, "일 정산"),
    FIFTEEN(15, "15일마다 정산"),
    THIRTY(30, "30일마다 정산");

    private final int days;
    private final String description;

    SettlementCycle(int days, String description) {
        this.days = days;
        this.description = description;
    }

    public static SettlementCycle from(int days) {
        return switch (days) {
            case 1 -> DAILY;
            case 15 -> FIFTEEN;
            case 30 -> THIRTY;
            default -> throw new IllegalArgumentException("Invalid settlement cycle: " + days);
        };
    }
}
