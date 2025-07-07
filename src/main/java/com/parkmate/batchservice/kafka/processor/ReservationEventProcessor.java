package com.parkmate.batchservice.kafka.processor;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.infrastructure.feignclient.ParkingLotInternalClient;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import com.parkmate.batchservice.hostsettlement.vo.response.ParkingLotHostUuidResponseVo;
import com.parkmate.batchservice.kafka.buffer.ReservationChunkBuffer;
import com.parkmate.batchservice.kafka.event.ReservationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProcessor {

    private final ReservationChunkBuffer reservationChunkBuffer;
    private final DailySettlementRepository dailySettlementRepository;
    private final ParkingLotInternalClient parkingLotInternalClient;

    public void process(ReservationEvent event) {
        reservationChunkBuffer.add(event);
        log.info("📥 예약 이벤트 버퍼 추가 완료: reservationCode={}, parkingLotUuid={}",
                event.getReservationCode(), event.getParkingLotUuid());

        String hostUuid = event.getHostUuid();
        if (hostUuid == null) {
            try {
                ParkingLotHostUuidResponseVo response = parkingLotInternalClient.getHostUuidByParkingLotUuid(event.getParkingLotUuid());
                hostUuid = response.getHostUuid();
            } catch (Exception e) {
                log.error("❌ hostUuid 조회 실패: parkingLotUuid={}, error={}", event.getParkingLotUuid(), e.getMessage(), e);
                return;
            }
        }

        if (hostUuid == null) {
            log.error("❌ hostUuid가 null입니다. reservationCode={}", event.getReservationCode());
            return;
        }

        if (dailySettlementRepository.existsByReservationCode(event.getReservationCode())) {
            log.info("⚠️ 이미 저장된 reservationCode: {}", event.getReservationCode());
            return;
        }

        try {
            DailySettlement daily = DailySettlement.builder()
                    .reservationCode(event.getReservationCode())
                    .hostUuid(hostUuid)
                    .parkingLotUuid(event.getParkingLotUuid())
                    .settlementDate(event.getTimestamp().toLocalDate())
                    .amount(java.math.BigDecimal.valueOf(event.getAmount()))
                    .status(event.getStatus().name())
                    .settlementCycle(SettlementCycle.DAILY)
                    .build();
            dailySettlementRepository.save(daily);
            log.info("✅ DailySettlement 저장 완료: reservationCode={}", event.getReservationCode());
        } catch (Exception e) {
            log.error("❌ DailySettlement 저장 실패: reservationCode={}, error={}", event.getReservationCode(), e.getMessage(), e);
        }
    }
}
