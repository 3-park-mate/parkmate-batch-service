package com.parkmate.batchservice.hostsettlement.application;

import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import com.parkmate.batchservice.kafka.event.ReservationEvent;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotSalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotWeeklySalesDto;
import com.parkmate.batchservice.hostsettlement.dto.response.WeeklySalesStatisticsDto;
import com.parkmate.batchservice.hostsettlement.dto.response.FlexibleWeeklyStatisticsDto;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface  HostSettlementService {

    void settle(ReservationEvent event);

    List<HostParkingLotDto> extractMonthlyTargetsFromDB(YearMonth yearMonth, SettlementCycle cycle);

    DailySalesResponseDto getDailySales(String hostUuid, String parkingLotUuid, LocalDate date);

    MonthlySalesResponseDto getMonthlySales(String hostUuid, String parkingLotUuid, int year, int month, SettlementCycle cycle);

    List<DailySalesSummaryDto> getDailySalesSummary(LocalDate startDate, LocalDate endDate);

    List<ParkingLotSalesSummaryDto> getParkingLotSalesSummary(String hostUuid, int year, int month, Integer week);

    List<ParkingLotWeeklySalesDto> getWeeklySalesAll(String hostUuid, int year, int week);

    List<ParkingLotWeeklySalesDto> getParkingLotsWeeklySalesByRange(String hostUuid, String startDate, String endDate);

    List<DailySalesResponseDto> getWeeklySalesByParkingLot(String hostUuid, String parkingLotUuid, int year, int week);

    List<DailySalesResponseDto> getWeeklySalesByParkingLotAndRange(String hostUuid, String parkingLotUuid, String startDate, String endDate);

    List<ParkingLotSalesSummaryDto> getParkingLotSalesSummaryByRange(String hostUuid, String startDate, String endDate);

    List<ParkingLotSalesSummaryDto> getParkingLotSalesSummaryFlexible(String hostUuid, int year, Integer month, Integer weekOfMonth);

    List<WeeklySalesStatisticsDto> getWeeklySalesStatistics(String hostUuid, int year, int month, int weekOfMonth);

    List<WeeklySalesStatisticsDto> getWeeklySalesStatisticsByRange(String hostUuid, String startDate, String endDate);

    FlexibleWeeklyStatisticsDto getFlexibleWeeklyStatistics(String hostUuid, String baseDate, Integer daysBefore, Integer daysAfter);
}
