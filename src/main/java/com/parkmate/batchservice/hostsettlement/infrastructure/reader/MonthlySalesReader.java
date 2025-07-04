package com.parkmate.batchservice.hostsettlement.infrastructure.reader;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforpayment.SettlementPaymentResponseDto;
import com.parkmate.batchservice.hostsettlement.infrastructure.client.PaymentFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@StepScope
public class MonthlySalesReader implements ItemReader<HostSettlement> {

    private final PaymentFeignClient paymentFeignClient;
    private final String hostUuid;
    private final String parkingLotUuid;
    private final YearMonth yearMonth;
    private final SettlementCycle settlementCycle;

    private boolean read = false;

    public MonthlySalesReader(PaymentFeignClient paymentFeignClient,
                              String hostUuid,
                              String parkingLotUuid,
                              int year,
                              int month,
                              SettlementCycle settlementCycle) {
        this.paymentFeignClient = paymentFeignClient;
        this.hostUuid = hostUuid;
        this.parkingLotUuid = parkingLotUuid;
        this.yearMonth = YearMonth.of(year, month);
        this.settlementCycle = settlementCycle;
    }

    @Override
    public HostSettlement read() {
        if (read) return null;
        read = true;

        LocalDate startDate = settlementCycle == SettlementCycle.FIFTEEN
                ? yearMonth.atDay(1)
                : yearMonth.atDay(16);

        LocalDate endDate = settlementCycle == SettlementCycle.FIFTEEN
                ? yearMonth.atDay(15)
                : yearMonth.atEndOfMonth();

        log.info("📦 [MonthlySalesReader] 조회 조건 - host={}, lot={}, start={}, end={}, cycle={}",
                hostUuid, parkingLotUuid, startDate, endDate, settlementCycle.name());

        List<SettlementPaymentResponseDto> payments;
        try {
            payments = paymentFeignClient.getSettlementPayments(
                    hostUuid,
                    parkingLotUuid,
                    startDate.toString(),
                    endDate.toString()
            );
        } catch (Exception e) {
            log.error("❌ [MonthlySalesReader] 결제 서비스 호출 실패: {}", e.getMessage(), e);
            return null;
        }

        if (payments.isEmpty()) {
            log.info("ℹ️ [MonthlySalesReader] 해당 기간 결제 내역 없음: {} ~ {}", startDate, endDate);
            return null;
        }

        BigDecimal totalAmount = payments.stream()
                .map(SettlementPaymentResponseDto::getAmount)
                .filter(amount -> amount != null)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return HostSettlement.builder()
                .hostUuid(hostUuid)
                .parkingLotUuid(parkingLotUuid)
                .settlementDate(endDate)
                .totalSalesAmount(totalAmount)
                .status("COMPLETED")
                .settlementCycle(settlementCycle)
                .build();
    }
}