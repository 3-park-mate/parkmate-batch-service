package com.parkmate.batchservice.hostsettlement.infrastructure.processor;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforpayment.SettlementPaymentResponseDto;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class DailySalesProcessor implements ItemProcessor<SettlementPaymentResponseDto, HostSettlement> {

    @Override
    public HostSettlement process(SettlementPaymentResponseDto dto) {

        BigDecimal amount = dto.getAmount() != null
                ? BigDecimal.valueOf(dto.getAmount())
                : BigDecimal.ZERO; // null 방어 로직

        return HostSettlement.builder()
                .hostUuid(dto.getHostUuid())
                .parkingLotUuid(dto.getParkingLotUuid())
                .settlementDate(dto.getPaymentDate().toLocalDate())
                .totalSalesAmount(amount)
                .status("COMPLETED")
                .settlementCycle(SettlementCycle.DAILY)
                .build();
    }
}