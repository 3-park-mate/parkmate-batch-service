package com.parkmate.batchservice.reviewsummary.infrastructure.repository;

import com.parkmate.batchservice.reviewsummary.domain.ReviewSummaryRealtime;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewSummaryRealtimeRepository extends JpaRepository<ReviewSummaryRealtime, Long> {

    Optional<ReviewSummaryRealtime> findByParkingLotUuid(String parkingLotUuid);
}
