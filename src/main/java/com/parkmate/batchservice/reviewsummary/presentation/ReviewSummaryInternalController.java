package com.parkmate.batchservice.reviewsummary.presentation;

import com.parkmate.batchservice.common.response.ApiResponse;
import com.parkmate.batchservice.reviewsummary.application.ReviewSummaryService;
import com.parkmate.batchservice.reviewsummary.dto.response.ReviewSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/batch/review-summary")
@RequiredArgsConstructor
public class ReviewSummaryInternalController {

    private final ReviewSummaryService reviewSummaryService;

    @GetMapping
    public ApiResponse<ReviewSummaryResponseDto> getReviewSummary(
            @RequestParam String parkingLotUuid
    ) {
        ReviewSummaryResponseDto dto = reviewSummaryService.getSummary(parkingLotUuid);

        return ApiResponse.of(
                HttpStatus.OK,
                "리뷰 요약 조회 성공",
                dto
        );
    }
}
