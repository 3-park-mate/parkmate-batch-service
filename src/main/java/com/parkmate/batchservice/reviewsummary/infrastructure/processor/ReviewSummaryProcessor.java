package com.parkmate.batchservice.reviewsummary.infrastructure.processor;

import com.parkmate.batchservice.kafka.event.ReviewCreatedJoinUserEvent;
import com.parkmate.batchservice.reviewsummary.domain.ReviewSummary;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ReviewSummaryProcessor implements ItemProcessor<ReviewCreatedJoinUserEvent, ReviewSummary> {
    @Override
    public ReviewSummary process(ReviewCreatedJoinUserEvent event) {
        return ReviewSummary.of(
                event.getParkingLotUuid(),
                event.getRating(),
                1L
        );
    }
}