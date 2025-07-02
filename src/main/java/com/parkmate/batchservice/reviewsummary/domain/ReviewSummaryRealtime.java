package com.parkmate.batchservice.reviewsummary.domain;

import com.parkmate.batchservice.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_summary_realtime")
public class ReviewSummaryRealtime extends BaseEntity {

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

    @Comment("마지막 업데이트 시각")
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Builder
    private ReviewSummaryRealtime(String parkingLotUuid,
                                  double averageRating,
                                  long totalReviews,
                                  LocalDateTime lastUpdatedAt) {
        this.parkingLotUuid = parkingLotUuid;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.lastUpdatedAt = lastUpdatedAt != null ? lastUpdatedAt : LocalDateTime.now();
    }

    public static ReviewSummaryRealtime of(String parkingLotUuid,
                                           double averageRating,
                                           long totalReviews) {
        return ReviewSummaryRealtime.builder()
                .parkingLotUuid(parkingLotUuid)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    public void update(double averageRating, long totalReviews) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}