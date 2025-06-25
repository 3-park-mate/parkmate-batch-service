package com.parkmate.batchservice.reviewsummary.infrastructure.writer;

import com.parkmate.batchservice.reviewsummary.domain.ReviewSummary;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSummaryWriter implements ItemWriter<ReviewSummary> {

    private final ReviewSummaryRepository reviewSummaryRepository;

    @Override
    public void write(Chunk<? extends ReviewSummary> items) {
        for (ReviewSummary newSummary : items) {
            ReviewSummary existingSummary = reviewSummaryRepository.findById(newSummary.getParkingLotUuid())
                    .orElse(null);

            if (existingSummary != null) {
                long totalReviews = existingSummary.getTotalReviews() + newSummary.getTotalReviews();

                double totalRatingSum = existingSummary.getAverageRating() * existingSummary.getTotalReviews()
                        + newSummary.getAverageRating() * newSummary.getTotalReviews();

                double averageRating = totalReviews > 0 ? totalRatingSum / totalReviews : 0.0;

                existingSummary.update(averageRating, totalReviews);

                reviewSummaryRepository.save(existingSummary);
            } else {
                reviewSummaryRepository.save(newSummary);
            }
        }
    }
}