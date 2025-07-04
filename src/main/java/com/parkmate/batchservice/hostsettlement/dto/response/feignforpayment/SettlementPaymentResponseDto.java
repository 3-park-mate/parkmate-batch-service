package com.parkmate.batchservice.hostsettlement.dto.response.feignforpayment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public class SettlementPaymentResponseDto {

    private String hostUuid;
    private String parkingLotUuid;

    @JsonProperty("totalAmount")
    private Long amount;

    @JsonProperty("approvedAt")
    private ZonedDateTime paymentDate;
}
