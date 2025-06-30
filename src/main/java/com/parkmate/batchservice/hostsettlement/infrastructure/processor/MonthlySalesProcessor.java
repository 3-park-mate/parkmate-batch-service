package com.parkmate.batchservice.hostsettlement.infrastructure.processor;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforpayment.SettlementPaymentResponseDto;
import org.springframework.batch.item.ItemProcessor;

public class MonthlySalesProcessor implements ItemProcessor<SettlementPaymentResponseDto, HostSettlement> {

    private final SettlementCycle settlementCycle;

    public MonthlySalesProcessor(SettlementCycle settlementCycle) {
        this.settlementCycle = settlementCycle;
    }

    @Override
    public HostSettlement process(SettlementPaymentResponseDto dto) {
        return HostSettlement.builder()
                .hostUuid(dto.getHostUuid())
                .parkingLotUuid(dto.getParkingLotUuid())
                .settlementDate(dto.getPaymentDate())
                .totalSalesAmount(dto.getAmount())
                .status("COMPLETED")
                .settlementCycle(settlementCycle)
                .build();
    }
}