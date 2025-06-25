package com.parkmate.batchservice.reviewsummary.infrastructure.repository;


import com.parkmate.batchservice.reviewsummary.domain.ReviewSummaryRealtime;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ReviewSummaryRealtimeRepository extends MongoRepository<ReviewSummaryRealtime, String> {

    Optional<ReviewSummaryRealtime> findByParkingLotUuid(String parkingLotUuid);
}
