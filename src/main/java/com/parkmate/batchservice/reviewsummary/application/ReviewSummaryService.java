package com.parkmate.batchservice.reviewsummary.application;

import com.parkmate.batchservice.reviewsummary.dto.response.ReviewSummaryResponseDto;

public interface ReviewSummaryService {

    ReviewSummaryResponseDto getSummary(String parkingLotUuid);
}
