package com.parkmate.batchservice.reviewsummary.infrastructure.repository;

import com.parkmate.batchservice.reviewsummary.domain.ReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewSummaryRepository extends JpaRepository<ReviewSummary, Long> {

    Optional<ReviewSummary> findByParkingLotUuid(String parkingLotUuid);
}