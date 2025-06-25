package com.parkmate.batchservice.reviewsummary.infrastructure.repository;

import com.parkmate.batchservice.reviewsummary.domain.ReviewSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ReviewSummaryRepository extends MongoRepository<ReviewSummary, String> {

    Optional<ReviewSummary> findByParkingLotUuid(String parkingLotUuid);
}
