package com.parkmate.batchservice.reviewsummary.application;

import com.parkmate.batchservice.kafka.event.ReviewCreatedJoinUserEvent;
import com.parkmate.batchservice.kafka.producer.ParkingLotRatingProducer;
import com.parkmate.batchservice.reviewsummary.domain.ReviewSummaryRealtime;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewChunkBuffer;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewSummaryRealtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewSummaryRealtimeService {

    private static final long REALTIME_THRESHOLD = 1000;

    private final ReviewSummaryRealtimeRepository reviewSummaryRealtimeRepository;
    private final ParkingLotRatingProducer parkingLotRatingProducer;

    public void processOrBuffer(ReviewCreatedJoinUserEvent event, ReviewChunkBuffer reviewChunkBuffer) {
        long currentReviewCount = reviewSummaryRealtimeRepository.findByParkingLotUuid(event.getParkingLotUuid())
                .map(ReviewSummaryRealtime::getTotalReviews)
                .orElse(0L);

        if (currentReviewCount < REALTIME_THRESHOLD) {

            saveOrUpdateReviewSummary(event);
        } else {

            reviewChunkBuffer.add(event);
        }
    }

    @Transactional
    public void saveOrUpdateReviewSummary(ReviewCreatedJoinUserEvent event) {
        ReviewSummaryRealtime updatedSummary = reviewSummaryRealtimeRepository
                .findByParkingLotUuid(event.getParkingLotUuid())
                .map(existing -> updateSummary(existing, event.getRating())) // 기존 데이터가 있으면 갱신
                .orElseGet(() -> createSummary(event));

        reviewSummaryRealtimeRepository.save(updatedSummary);

        parkingLotRatingProducer.sendRatingUpdate(
                updatedSummary.getParkingLotUuid(),
                updatedSummary.getAverageRating()
        );
    }

    private ReviewSummaryRealtime updateSummary(ReviewSummaryRealtime existing, int newRating) {
        long newTotalReviews = existing.getTotalReviews() + 1;
        double newAverageRating = calculateNewAverage(
                existing.getAverageRating(),
                existing.getTotalReviews(),
                newRating
        );

        existing.update(newAverageRating, newTotalReviews);
        return existing;
    }

    private ReviewSummaryRealtime createSummary(ReviewCreatedJoinUserEvent event) {
        return ReviewSummaryRealtime.of(
                event.getParkingLotUuid(),
                event.getRating(),
                1L
        );
    }

    private double calculateNewAverage(double currentAverage, long currentCount, int newRating) {
        double totalRatingSum = currentAverage * currentCount + newRating;
        return totalRatingSum / (currentCount + 1);
    }
}