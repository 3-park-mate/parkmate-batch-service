package com.parkmate.batchservice.hostsettlement.application;

import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import com.parkmate.batchservice.kafka.buffer.ReservationChunkBuffer;
import com.parkmate.batchservice.kafka.event.ReservationEvent;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface  HostSettlementService {


    /**
     * 실시간 예약 이벤트 기반 정산 처리 (일 정산용)
     */
    void settle(ReservationEvent event);

    /**
     * 월 정산 대상 주차장-호스트 쌍 추출 (DB 기반)
     */
    List<HostParkingLotDto> extractMonthlyTargetsFromDB(YearMonth yearMonth, SettlementCycle cycle);

    /**
     * 일별 매출 조회 (Feign API 제공)
     */
    DailySalesResponseDto getDailySales(String hostUuid, String parkingLotUuid, LocalDate date);

    /**
     * 월별 매출 조회 (Feign API 제공)
     */
    MonthlySalesResponseDto getMonthlySales(String hostUuid, String parkingLotUuid, int year, int month, SettlementCycle cycle);

    /**
     * 일매출 합산 조회 (날짜+호스트+주차장별)
     */
    List<DailySalesSummaryDto> getDailySalesSummary(LocalDate startDate, LocalDate endDate);
}
