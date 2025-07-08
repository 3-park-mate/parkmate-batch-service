package com.parkmate.batchservice.kafka.producer;

import com.parkmate.batchservice.kafka.event.ParkingLotRatingUpdatedEvent;
import com.parkmate.batchservice.reviewsummary.application.ReviewSummaryService;
import com.parkmate.batchservice.reviewsummary.dto.response.ReviewSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingLotRatingProducer {

    private final KafkaTemplate<String, ParkingLotRatingUpdatedEvent> kafkaTemplate;
    private final ReviewSummaryService reviewSummaryService;
    private static final String TOPIC = "batch.review-summary.updated";

    public void sendRatingUpdate(String parkingLotUuid, double averageRating) {
        // 리뷰 요약 정보 조회
        ReviewSummaryResponseDto summary = reviewSummaryService.getSummary(parkingLotUuid);
        double roundedRating = Math.round(summary.getAverageRating() * 10.0) / 10.0;
        long totalReview = summary.getTotalReviews();

        ParkingLotRatingUpdatedEvent event = ParkingLotRatingUpdatedEvent.builder()
                .parkingLotUuid(parkingLotUuid)
                .averageRating(roundedRating)
                .totalReview(totalReview)
                .build();

        kafkaTemplate.send(TOPIC, parkingLotUuid, event);
        log.info("[Kafka Produce] 주차장 평점 업데이트 전송: {}", event);
    }
}
