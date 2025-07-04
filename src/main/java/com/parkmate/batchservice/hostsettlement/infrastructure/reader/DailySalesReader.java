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
import java.util.List;

@Slf4j
@StepScope
public class DailySalesReader implements ItemReader<HostSettlement> {

    private final PaymentFeignClient paymentFeignClient;
    private final String hostUuid;
    private final String parkingLotUuid;
    private final LocalDate targetDate;

    private boolean read = false;

    public DailySalesReader(PaymentFeignClient paymentFeignClient,
                            String hostUuid,
                            String parkingLotUuid,
                            String targetDate) {
        this.paymentFeignClient = paymentFeignClient;
        this.hostUuid = hostUuid;
        this.parkingLotUuid = parkingLotUuid;
        this.targetDate = LocalDate.parse(targetDate);
    }

    @Override
    public HostSettlement read() {
        if (read) return null;
        read = true;

        log.info("📦 [DailySalesReader] 정산 데이터 조회: host={}, lot={}, date={}", hostUuid, parkingLotUuid, targetDate);


        List<SettlementPaymentResponseDto> payments;
        try {
            payments = paymentFeignClient.getSettlementPayments(
                    hostUuid,
                    parkingLotUuid,
                    targetDate.toString(),
                    targetDate.toString()
            );

            log.info("✅ [DailySalesReader] 조회된 결제 건수: {}", payments.size());
            for (SettlementPaymentResponseDto p : payments) {
                log.info("📄 결제 데이터 - host: {}, lot: {}, amount: {}, date: {}",
                        p.getHostUuid(),
                        p.getParkingLotUuid(),
                        p.getAmount(),
                        p.getPaymentDate());

            }

        } catch (Exception e) {
            log.error("❌ [DailySalesReader] 결제 서비스 호출 실패: {}", e.getMessage(), e);
            return null;
        }

        if (payments.isEmpty()) {
            log.info("ℹ️ [DailySalesReader] 해당 날짜에 결제 내역 없음: {}", targetDate);
            return null;
        }

        BigDecimal totalAmount = payments.stream()
                .map(SettlementPaymentResponseDto::getAmount) // Long
                .filter(amount -> amount != null)              // null 체크
                .map(BigDecimal::valueOf)                      // Long → BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add);     // 합계 계산

        log.info("💰 [DailySalesReader] 계산된 총 정산 금액: {}", totalAmount);

        return HostSettlement.builder()
                .hostUuid(hostUuid)
                .parkingLotUuid(parkingLotUuid)
                .settlementDate(targetDate)
                .totalSalesAmount(totalAmount)
                .status("COMPLETED")
                .settlementCycle(SettlementCycle.DAILY)
                .build();
    }
}