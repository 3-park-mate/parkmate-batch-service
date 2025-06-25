package com.parkmate.batchservice.reviewsummary.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reviewsummary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewSummary {

    @Id
    private String parkingLotUuid;

    private double averageRating;
    private long totalReviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private ReviewSummary(String parkingLotUuid,
                         double averageRating,
                         long totalReviews) {
        this.parkingLotUuid = parkingLotUuid;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ReviewSummary of(String parkingLotUuid,
                                   double averageRating,
                                   long totalReviews) {
        return ReviewSummary.builder()
                .parkingLotUuid(parkingLotUuid)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }

    public void update(double averageRating, long totalReviews) {

        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.updatedAt = LocalDateTime.now();
    }
}
