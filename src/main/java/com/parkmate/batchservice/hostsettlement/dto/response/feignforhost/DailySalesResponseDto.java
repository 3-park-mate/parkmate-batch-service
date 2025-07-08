package com.parkmate.batchservice.hostsettlement.dto.response.feignforhost;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySalesResponseDto {
    private LocalDate date;
    @JsonIgnore
    private BigDecimal amount;

    public DailySalesResponseDto(LocalDate date, BigDecimal amount) {
        this.date = date;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public LocalDate getDate() {
        return date;
    }

    @JsonProperty("amount")
    public int getAmount() {
        return amount.intValue();
    }
}
