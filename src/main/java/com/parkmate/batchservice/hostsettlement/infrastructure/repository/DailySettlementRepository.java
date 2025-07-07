package com.parkmate.batchservice.hostsettlement.infrastructure.repository;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {
    boolean existsByReservationCode(String reservationCode);
    List<DailySettlement> findByHostUuidAndParkingLotUuidAndSettlementDate(String hostUuid, String parkingLotUuid, LocalDate settlementDate);
    List<DailySettlement> findByHostUuidAndParkingLotUuidAndSettlementDateBetweenAndSettlementCycle(String hostUuid, String parkingLotUuid, LocalDate startDate, LocalDate endDate, SettlementCycle settlementCycle);
    List<DailySettlement> findBySettlementDateBetweenAndSettlementCycle(LocalDate startDate, LocalDate endDate, SettlementCycle settlementCycle);
    @Query("SELECT new com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto(" +
           "d.hostUuid, d.parkingLotUuid, d.settlementDate, SUM(d.amount)) " +
           "FROM DailySettlement d " +
           "WHERE d.settlementDate BETWEEN :startDate AND :endDate " +
           "GROUP BY d.hostUuid, d.parkingLotUuid, d.settlementDate")
    List<DailySalesSummaryDto> findDailySalesSummary(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
} 