package com.parkmate.batchservice.hostsettlement.infrastructure.repository;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface HostSettlementRepository extends JpaRepository<HostSettlement, Long> {

    boolean existsByHostUuidAndParkingLotUuidAndSettlementDateAndSettlementCycle(String hostUuid, String parkingLotUuid, LocalDate date, SettlementCycle cycle);

    List<HostSettlement> findByHostUuidAndParkingLotUuidAndSettlementDate(
            String hostUuid,
            String parkingLotUuid,
            LocalDate settlementDate
    );

    List<HostSettlement> findByHostUuidAndParkingLotUuidAndSettlementDateBetweenAndSettlementCycle(
            String hostUuid,
            String parkingLotUuid,
            LocalDate startDate,
            LocalDate endDate,
            SettlementCycle settlementCycle
    );
}
