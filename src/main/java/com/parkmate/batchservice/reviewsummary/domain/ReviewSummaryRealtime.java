package com.parkmate.batchservice.reviewsummary.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviewsummaryrealtime")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewSummaryRealtime {

    @Id
    private String parkingLotUuid;

    private double averageRating;
    private long totalReviews;
    private LocalDateTime lastUpdatedAt;

    @Builder
    private ReviewSummaryRealtime(String parkingLotUuid,
                                 double averageRating,
                                 long totalReviews) {
        this.parkingLotUuid = parkingLotUuid;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public static ReviewSummaryRealtime of(String parkingLotUuid,
                                           double averageRating,
                                           long totalReviews) {

        return ReviewSummaryRealtime.builder()
                .parkingLotUuid(parkingLotUuid)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }

    public void update(double averageRating, long totalReviews) {

        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
