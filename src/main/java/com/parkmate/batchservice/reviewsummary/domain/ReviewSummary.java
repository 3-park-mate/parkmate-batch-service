package com.parkmate.batchservice.reviewsummary.domain;

import com.parkmate.batchservice.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import jakarta.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_summary")
public class ReviewSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("주차장 UUID")
    @Column(nullable = false, unique = true, length = 36)
    private String parkingLotUuid;

    @Comment("리뷰 평균 평점")
    @Column(nullable = false)
    private double averageRating;

    @Comment("총 리뷰 수")
    @Column(nullable = false)
    private long totalReviews;

    @Builder
    private ReviewSummary(String parkingLotUuid,
                          double averageRating,
                          long totalReviews) {

        this.parkingLotUuid = parkingLotUuid;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
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
    }
}