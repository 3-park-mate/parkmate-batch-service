package com.parkmate.batchservice.reviewsummary.infrastructure.writer;

import com.parkmate.batchservice.reviewsummary.domain.ReviewSummary;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSummaryWriter implements ItemWriter<ReviewSummary> {

    private final ReviewSummaryRepository reviewSummaryRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends ReviewSummary> items) {
        for (ReviewSummary newSummary : items) {
            reviewSummaryRepository.findByParkingLotUuid(newSummary.getParkingLotUuid())
                    .ifPresentOrElse(
                            existing -> {
                                long totalReviews = existing.getTotalReviews() + newSummary.getTotalReviews();
                                double totalRatingSum =
                                        existing.getAverageRating() * existing.getTotalReviews() +
                                                newSummary.getAverageRating() * newSummary.getTotalReviews();

                                double averageRating = totalReviews > 0 ? totalRatingSum / totalReviews : 0.0;
                                existing.update(averageRating, totalReviews);

                                // ✅ 명시적 save (영속성 보장)
                                reviewSummaryRepository.save(existing);
                            },
                            () -> {
                                reviewSummaryRepository.save(newSummary);
                            }
                    );
        }
    }
}