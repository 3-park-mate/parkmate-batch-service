package com.parkmate.batchservice.reviewsummary.application;

import com.parkmate.batchservice.reviewsummary.domain.ReviewSummary;
import com.parkmate.batchservice.reviewsummary.domain.ReviewSummaryRealtime;
import com.parkmate.batchservice.reviewsummary.dto.response.ReviewSummaryResponseDto;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewSummaryRealtimeRepository;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewSummaryServiceImpl implements ReviewSummaryService {

    private final ReviewSummaryRepository reviewSummaryRepository;
    private final ReviewSummaryRealtimeRepository realtimeRepository;

    @Override
    public ReviewSummaryResponseDto getSummary(String parkingLotUuid) {
        return combineSummaries(parkingLotUuid);
    }

    private ReviewSummaryResponseDto combineSummaries(String parkingLotUuid) {

        Optional<ReviewSummaryRealtime> realtimeOpt = realtimeRepository.findByParkingLotUuid(parkingLotUuid);
        Optional<ReviewSummary> batchOpt = reviewSummaryRepository.findByParkingLotUuid(parkingLotUuid);

        long realtimeCount = realtimeOpt.map(ReviewSummaryRealtime::getTotalReviews).orElse(0L);
        long batchCount = batchOpt.map(ReviewSummary::getTotalReviews).orElse(0L);
        double realtimeAvg = realtimeOpt.map(ReviewSummaryRealtime::getAverageRating).orElse(0.0);
        double batchAvg = batchOpt.map(ReviewSummary::getAverageRating).orElse(0.0);

        long totalCount = realtimeCount + batchCount;

        double combinedAvg = totalCount > 0
                ? (realtimeAvg * realtimeCount + batchAvg * batchCount) / totalCount
                : 0.0;

        return ReviewSummaryResponseDto.from(combinedAvg, totalCount);
    }
}