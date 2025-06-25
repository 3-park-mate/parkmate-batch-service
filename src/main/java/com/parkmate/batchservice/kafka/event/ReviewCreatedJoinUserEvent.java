package com.parkmate.batchservice.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ReviewCreatedJoinUserEvent {

    private String reviewUuid;
    private String userUuid;
    private String name;
    private String parkingLotUuid;
    private String content;
    private List<String> imageUrls;
    private int rating;
    private int likeCount;
    private int dislikeCount;
    private LocalDateTime createdAt;

    @Builder
    private ReviewCreatedJoinUserEvent(String reviewUuid,
                                     String userUuid,
                                     String name,
                                     String parkingLotUuid,
                                     String content,
                                     List<String> imageUrls,
                                     int rating,
                                     int likeCount,
                                     int dislikeCount,
                                     LocalDateTime createdAt) {
        this.reviewUuid = reviewUuid;
        this.userUuid = userUuid;
        this.name = name;
        this.parkingLotUuid = parkingLotUuid;
        this.content = content;
        this.imageUrls = imageUrls;
        this.rating = rating;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.createdAt = createdAt;
    }
}
