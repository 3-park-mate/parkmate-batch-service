package com.parkmate.batchservice.hostsettlement.infrastructure.processor;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforpayment.SettlementPaymentResponseDto;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class MonthlySalesProcessor implements ItemProcessor<SettlementPaymentResponseDto, HostSettlement> {

    private final SettlementCycle settlementCycle;

    public MonthlySalesProcessor(SettlementCycle settlementCycle) {
        this.settlementCycle = settlementCycle;
    }

    @Override
    public HostSettlement process(SettlementPaymentResponseDto dto) {

        BigDecimal amount = dto.getAmount() != null
                ? BigDecimal.valueOf(dto.getAmount())
                : BigDecimal.ZERO; // null 방지

        return HostSettlement.builder()
                .hostUuid(dto.getHostUuid())
                .parkingLotUuid(dto.getParkingLotUuid())
                .settlementDate(dto.getPaymentDate().toLocalDate())
                .totalSalesAmount(amount)
                .status("COMPLETED")
                .settlementCycle(settlementCycle)
                .build();
    }
}