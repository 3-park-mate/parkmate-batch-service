package com.parkmate.batchservice.kafka.producer;

import com.parkmate.batchservice.kafka.event.ParkingLotRatingUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingLotRatingProducer {

    private final KafkaTemplate<String, ParkingLotRatingUpdatedEvent> kafkaTemplate;
    private static final String TOPIC = "batch.review-summary.updated";

    public void sendRatingUpdate(String parkingLotUuid, double averageRating) {
        ParkingLotRatingUpdatedEvent event = ParkingLotRatingUpdatedEvent.builder()
                .parkingLotUuid(parkingLotUuid)
                .averageRating(averageRating)
                .build();

        kafkaTemplate.send(TOPIC, parkingLotUuid, event);
        log.info("[Kafka Produce] 주차장 평점 업데이트 전송: {}", event);
    }
}
