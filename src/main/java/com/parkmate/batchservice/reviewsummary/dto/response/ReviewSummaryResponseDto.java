package com.parkmate.batchservice.reviewsummary.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewSummaryResponseDto {

    private double averageRating;
    private long totalReviews;

    @Builder
    private ReviewSummaryResponseDto(double averageRating,
                                     long totalReviews) {

        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public static ReviewSummaryResponseDto from(double averageRating, long totalReviews) {
        return ReviewSummaryResponseDto.builder()
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }
}
