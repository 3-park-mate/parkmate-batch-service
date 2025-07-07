package com.parkmate.batchservice.hostsettlement.infrastructure.processor;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.MonthlySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MonthlySalesProcessor implements ItemProcessor<List<DailySettlement>, MonthlySettlement> {
    private final SettlementCycle settlementCycle;

    @Override
    public MonthlySettlement process(List<DailySettlement> group) {
        if (group == null || group.isEmpty()) {
            log.warn("⚠️ [MonthlyProcessor] null 또는 빈 그룹 수신 - 스킵");
            return null;
        }
        String hostUuid = group.get(0).getHostUuid();
        String parkingLotUuid = group.get(0).getParkingLotUuid();
        // 집계 마지막일을 settlement_date로 사용
        java.time.LocalDate settlementDate = group.stream()
                .map(DailySettlement::getSettlementDate)
                .max(java.time.LocalDate::compareTo)
                .orElse(group.get(0).getSettlementDate());
        BigDecimal totalAmount = group.stream()
                .map(DailySettlement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String settlementCode = String.format(
            "%s-%s-%s-%s",
            hostUuid,
            parkingLotUuid,
            settlementDate.format(DateTimeFormatter.ofPattern("yyyyMM")),
            this.settlementCycle.name()
        );
        MonthlySettlement monthly = MonthlySettlement.builder()
                .settlementCode(settlementCode)
                .hostUuid(hostUuid)
                .parkingLotUuid(parkingLotUuid)
                .settlementDate(settlementDate)
                .totalAmount(totalAmount)
                .status("COMPLETED")
                .settlementCycle(this.settlementCycle)
                .build();
        log.debug("✅ [MonthlyProcessor] 월매출 집계 생성 완료 - host={}, lot={}, date={}, amount={}",
                hostUuid, parkingLotUuid, settlementDate, totalAmount);
        return monthly;
    }
}