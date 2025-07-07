package com.parkmate.batchservice.hostsettlement.infrastructure.repository;

import com.parkmate.batchservice.hostsettlement.domain.MonthlySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MonthlySettlementRepository extends JpaRepository<MonthlySettlement, Long> {
    boolean existsBySettlementCode(String settlementCode);
    List<MonthlySettlement> findByHostUuidAndParkingLotUuidAndSettlementDate(String hostUuid, String parkingLotUuid, LocalDate settlementDate);
    List<MonthlySettlement> findByHostUuidAndParkingLotUuidAndSettlementDateBetweenAndSettlementCycle(String hostUuid, String parkingLotUuid, LocalDate startDate, LocalDate endDate, SettlementCycle settlementCycle);
} 