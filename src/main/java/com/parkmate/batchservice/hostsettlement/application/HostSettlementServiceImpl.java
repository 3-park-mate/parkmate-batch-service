package com.parkmate.batchservice.hostsettlement.application;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.MonthlySettlement;
import com.parkmate.batchservice.hostsettlement.domain.ReservationStatus;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.MonthlySettlementRepository;
import com.parkmate.batchservice.kafka.event.ReservationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostSettlementServiceImpl implements HostSettlementService {

    private final DailySettlementRepository dailySettlementRepository;
    private final MonthlySettlementRepository monthlySettlementRepository;

    /**
     * ✅ 일 정산 처리 (Kafka Consumer를 통한 실시간 정산)
     */
    @Transactional
    @Override
    public void settle(ReservationEvent event) {
        if (event == null || event.getStatus() != ReservationStatus.CONFIRMED) {
            log.warn("❌ 정산 제외 대상 또는 잘못된 이벤트: {}", event != null ? event.getReservationCode() : "null");
            return;
        }
        if (dailySettlementRepository.existsByReservationCode(event.getReservationCode())) {
            log.info("⚠️ 이미 정산된 예약: {}", event.getReservationCode());
            return;
        }
        DailySettlement daily = DailySettlement.builder()
                .reservationCode(event.getReservationCode())
                .hostUuid(event.getHostUuid())
                .parkingLotUuid(event.getParkingLotUuid())
                .settlementDate(event.getTimestamp().toLocalDate())
                .amount(BigDecimal.valueOf(event.getAmount()))
                .status(event.getStatus().name())
                .settlementCycle(SettlementCycle.DAILY)
                .build();
        dailySettlementRepository.save(daily);
        log.info("✅ 일매출 저장 완료: {}", event.getReservationCode());
    }

    /**
     * ✅ 월 정산 대상 주차장-호스트 조합 추출 (DB 기반)
     */
    @Transactional(readOnly = true)
    @Override
    public List<HostParkingLotDto> extractMonthlyTargetsFromDB(YearMonth yearMonth, SettlementCycle cycle) {
        LocalDate start = (cycle == SettlementCycle.FIFTEEN) ? yearMonth.atDay(1) : yearMonth.atDay(16);
        LocalDate end = (cycle == SettlementCycle.FIFTEEN) ? yearMonth.atDay(15) : yearMonth.atEndOfMonth();
        // 집계 대상은 settlementDate, settlementCycle만으로 추출
        return dailySettlementRepository
                .findBySettlementDateBetweenAndSettlementCycle(start, end, SettlementCycle.DAILY)
                .stream()
                .map(d -> new HostParkingLotDto(d.getHostUuid(), d.getParkingLotUuid()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * ✅ 일별 매출 조회 (FeignClient)
     */
    @Transactional(readOnly = true)
    @Override
    public DailySalesResponseDto getDailySales(String hostUuid, String parkingLotUuid, LocalDate date) {
        BigDecimal total = dailySettlementRepository
                .findByHostUuidAndParkingLotUuidAndSettlementDate(hostUuid, parkingLotUuid, date)
                .stream()
                .map(DailySettlement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DailySalesResponseDto(date, total);
    }

    /**
     * ✅ 월별 매출 조회 (FeignClient)
     */
    @Transactional(readOnly = true)
    @Override
    public MonthlySalesResponseDto getMonthlySales(String hostUuid, String parkingLotUuid, int year, int month, SettlementCycle cycle) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = (cycle == SettlementCycle.FIFTEEN) ? ym.atDay(1) : ym.atDay(16);
        LocalDate end = (cycle == SettlementCycle.FIFTEEN) ? ym.atDay(15) : ym.atEndOfMonth();
        BigDecimal total = monthlySettlementRepository
                .findByHostUuidAndParkingLotUuidAndSettlementDateBetweenAndSettlementCycle(
                        hostUuid, parkingLotUuid, start, end, cycle)
                .stream()
                .map(MonthlySettlement::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MonthlySalesResponseDto(ym.toString(), total);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DailySalesSummaryDto> getDailySalesSummary(LocalDate startDate, LocalDate endDate) {
        return dailySettlementRepository.findDailySalesSummary(startDate, endDate);
    }
}