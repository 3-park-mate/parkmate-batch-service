package com.parkmate.batchservice.hostsettlement.dto.response.feignforpayment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SettlementPaymentResponseDto {

    private String hostUuid;
    private String parkingLotUuid;
    private BigDecimal amount;
    private LocalDate paymentDate;
}
