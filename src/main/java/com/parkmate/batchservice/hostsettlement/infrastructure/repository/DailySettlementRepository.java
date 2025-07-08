package com.parkmate.batchservice.hostsettlement.infrastructure.repository;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
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
    @Query("SELECT DISTINCT d.parkingLotUuid FROM DailySettlement d WHERE d.hostUuid = :hostUuid")
    List<String> findDistinctParkingLotUuidsByHostUuid(@Param("hostUuid") String hostUuid);
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM DailySettlement d WHERE d.hostUuid = :hostUuid AND d.parkingLotUuid = :parkingLotUuid AND FUNCTION('YEAR', d.settlementDate) = :year AND FUNCTION('MONTH', d.settlementDate) = :month")
    int sumMonthlySales(@Param("hostUuid") String hostUuid, @Param("parkingLotUuid") String parkingLotUuid, @Param("year") int year, @Param("month") int month);
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM DailySettlement d WHERE d.hostUuid = :hostUuid AND d.parkingLotUuid = :parkingLotUuid AND FUNCTION('YEAR', d.settlementDate) = :year AND FUNCTION('WEEK', d.settlementDate) = :week")
    int sumWeeklySales(@Param("hostUuid") String hostUuid, @Param("parkingLotUuid") String parkingLotUuid, @Param("year") int year, @Param("week") int week);
    @Query("SELECT new com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto(d.settlementDate, d.amount) " +
           "FROM DailySettlement d " +
           "WHERE d.parkingLotUuid = :parkingLotUuid AND FUNCTION('YEAR', d.settlementDate) = :year AND FUNCTION('WEEK', d.settlementDate) = :week")
    List<DailySalesResponseDto> findDailySalesByParkingLotAndYearWeek(@Param("parkingLotUuid") String parkingLotUuid, @Param("year") int year, @Param("week") int week);
    @Query("SELECT new com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto(d.settlementDate, d.amount) " +
           "FROM DailySettlement d " +
           "WHERE d.parkingLotUuid = :parkingLotUuid AND d.settlementDate BETWEEN :startDate AND :endDate")
    List<DailySalesResponseDto> findDailySalesByParkingLotAndDateRange(@Param("parkingLotUuid") String parkingLotUuid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
} 