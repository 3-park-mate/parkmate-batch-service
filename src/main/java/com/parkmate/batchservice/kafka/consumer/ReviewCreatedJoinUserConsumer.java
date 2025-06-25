package com.parkmate.batchservice.kafka.consumer;

import com.parkmate.batchservice.kafka.event.ReviewCreatedJoinUserEvent;
import com.parkmate.batchservice.reviewsummary.application.ReviewSummaryRealtimeService;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewChunkBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCreatedJoinUserConsumer {

    private final ReviewSummaryRealtimeService realtimeService;
    private final ReviewChunkBuffer reviewChunkBuffer;

    @KafkaListener(topics = "user.review-join-user.created", groupId = "review-summary-group")
    public void consume(ReviewCreatedJoinUserEvent event) {
        log.info("[Kafka Consume] 리뷰 조인 이벤트 수신: {}", event);

        realtimeService.processOrBuffer(event, reviewChunkBuffer);
    }
}