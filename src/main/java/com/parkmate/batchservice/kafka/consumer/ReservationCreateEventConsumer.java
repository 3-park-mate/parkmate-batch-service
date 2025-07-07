package com.parkmate.batchservice.kafka.consumer;

import com.parkmate.batchservice.hostsettlement.domain.ReservationStatus;
import com.parkmate.batchservice.kafka.event.ReservationEvent;
import com.parkmate.batchservice.kafka.processor.ReservationEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCreateEventConsumer {

    private final ReservationEventProcessor reservationEventProcessor;

    @KafkaListener(
            topics = "reservation.reservation",
            containerFactory = "reservationKafkaListenerContainerFactory"
    )
    public void listen(@Payload ReservationEvent event, ConsumerRecord<String, ReservationEvent> record) {
        log.info("📥 [Kafka] 예약 생성 이벤트 수신: key={}, offset={}, partition={}, event={}",
                record.key(), record.offset(), record.partition(), event);

        ReservationStatus status = event.getStatus();

        if (status == null) {
            log.warn("⚠️ [Kafka] 예약 상태 누락: reservationCode={}", event.getReservationCode());
            return;
        }

        if (status != ReservationStatus.CONFIRMED) {
            log.info("⚠️ [Kafka] 정산 대상 아님 (예약 상태: {}): reservationCode={}", status, event.getReservationCode());
            return;
        }

        try {
            reservationEventProcessor.process(event);
        } catch (Exception e) {
            log.error("❌ [Kafka] 예약 이벤트 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}