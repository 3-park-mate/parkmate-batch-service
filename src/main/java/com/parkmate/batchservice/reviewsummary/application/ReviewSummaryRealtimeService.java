package com.parkmate.batchservice.reviewsummary.application;

import com.parkmate.batchservice.kafka.event.ReviewCreatedJoinUserEvent;
import com.parkmate.batchservice.reviewsummary.domain.ReviewSummaryRealtime;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewChunkBuffer;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewSummaryRealtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSummaryRealtimeService {
    private static final long REALTIME_THRESHOLD = 1000;

    private final ReviewSummaryRealtimeRepository reviewSummaryRealtimeRepository;

    public void processOrBuffer(ReviewCreatedJoinUserEvent event, ReviewChunkBuffer reviewChunkBuffer) {
        long currentReviewCount = reviewSummaryRealtimeRepository.findByParkingLotUuid(event.getParkingLotUuid())
                .map(ReviewSummaryRealtime::getTotalReviews)
                .orElse(0L);

        if (currentReviewCount < REALTIME_THRESHOLD) {
            saveOrUpdateReviewSummary(event);
        } else {
            reviewChunkBuffer.add(event);
            log.info("[버퍼 저장] 주차장 UUID: {}, 현재 리뷰 수: {}, 버퍼 사이즈: {}",
                    event.getParkingLotUuid(),
                    currentReviewCount,
                    reviewChunkBuffer.size());
        }
    }

    @Transactional
    public void saveOrUpdateReviewSummary(ReviewCreatedJoinUserEvent event) {
        var optionalSummary = reviewSummaryRealtimeRepository.findByParkingLotUuid(event.getParkingLotUuid());

        ReviewSummaryRealtime updatedSummary = optionalSummary
                .map(existingSummary -> updateSummary(existingSummary, event.getRating()))
                .orElseGet(() -> createSummary(event));

        reviewSummaryRealtimeRepository.save(updatedSummary);

        log.info("[실시간 저장] 주차장 UUID: {}, 평균 평점: {}, 리뷰 수: {}",
                updatedSummary.getParkingLotUuid(),
                updatedSummary.getAverageRating(),
                updatedSummary.getTotalReviews());
    }

    private ReviewSummaryRealtime updateSummary(ReviewSummaryRealtime existing, int newRating) {
        long newTotalReviews = existing.getTotalReviews() + 1;
        double newAverageRating = calculateNewAverage(existing.getAverageRating(), existing.getTotalReviews(), newRating);

        existing.update(newAverageRating, newTotalReviews);
        return existing;
    }

    private ReviewSummaryRealtime createSummary(ReviewCreatedJoinUserEvent event) {
        return ReviewSummaryRealtime.of(event.getParkingLotUuid(), event.getRating(), 1L);
    }

    private double calculateNewAverage(double currentAverage, long currentCount, int newRating) {
        double totalRatingSum = currentAverage * currentCount + newRating;
        return totalRatingSum / (currentCount + 1);
    }
}