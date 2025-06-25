package com.parkmate.batchservice.reviewsummary.presentation;

import com.parkmate.batchservice.common.response.ApiResponse;
import com.parkmate.batchservice.reviewsummary.application.ReviewSummaryService;
import com.parkmate.batchservice.reviewsummary.dto.response.ReviewSummaryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewSummaryController {

    private final ReviewSummaryService reviewSummaryService;

    @Operation(
            summary = "리뷰 집계 조회 (실시간 + 배치 합산)",
            description = "해당 주차장의 실시간 리뷰 데이터와 배치 리뷰 데이터를 합산하여 평점 평균과 리뷰 총 개수를 조회합니다.",
            tags = {"REVIEW-SUMMARY"}
    )
    @GetMapping("/summary")
    public ApiResponse<ReviewSummaryResponseDto> getReviewSummary(@RequestParam String parkingLotUuid) {
        ReviewSummaryResponseDto response = reviewSummaryService.getSummary(parkingLotUuid);

        return ApiResponse.of(
                HttpStatus.OK,
                "요청에 성공하였습니다.",
                response
        );
    }
}
