package com.parkmate.batchservice.kafka.processor;

import com.parkmate.batchservice.hostsettlement.application.HostSettlementService;
import com.parkmate.batchservice.hostsettlement.infrastructure.feignclient.ParkingLotInternalClient;
import com.parkmate.batchservice.hostsettlement.vo.response.ParkingLotHostUuidResponseVo;
import com.parkmate.batchservice.kafka.buffer.ReservationChunkBuffer;
import com.parkmate.batchservice.kafka.event.ReservationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProcessor {

    private final ReservationChunkBuffer reservationChunkBuffer;
    private final HostSettlementService hostSettlementService;
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

        try {
            hostSettlementService.settle(event);
            log.info("✅ 정산 처리 완료: reservationCode={}", event.getReservationCode());
        } catch (Exception e) {
            log.error("❌ 정산 처리 실패: reservationCode={}, error={}", event.getReservationCode(), e.getMessage(), e);
        }
    }
}
