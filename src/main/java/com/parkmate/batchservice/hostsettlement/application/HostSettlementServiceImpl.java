package com.parkmate.batchservice.hostsettlement.application;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.MonthlySettlement;
import com.parkmate.batchservice.hostsettlement.domain.ReservationStatus;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.DailySalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotSalesSummaryDto;
import com.parkmate.batchservice.hostsettlement.dto.response.ParkingLotWeeklySalesDto;
import com.parkmate.batchservice.hostsettlement.dto.response.WeeklySalesStatisticsDto;
import com.parkmate.batchservice.hostsettlement.dto.response.FlexibleWeeklyStatisticsDto;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.MonthlySettlementRepository;
import com.parkmate.batchservice.hostsettlement.vo.response.ParkingLotInfoResponseVo;
import com.parkmate.batchservice.kafka.event.ReservationEvent;
import com.parkmate.batchservice.hostsettlement.infrastructure.feignclient.ParkingLotInternalClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import com.parkmate.batchservice.common.response.ApiResponse;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostSettlementServiceImpl implements HostSettlementService {

    private final DailySettlementRepository dailySettlementRepository;
    private final MonthlySettlementRepository monthlySettlementRepository;
    private final ParkingLotInternalClient parkingLotInternalClient;

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

    @Override
    public List<ParkingLotSalesSummaryDto> getParkingLotSalesSummary(String hostUuid, int year, int month, Integer week) {
        // 1. hostUuid가 관리하는 모든 주차장 UUID 리스트 조회 (일매출 테이블에서 distinct 추출)
        List<String> parkingLotUuids = dailySettlementRepository.findDistinctParkingLotUuidsByHostUuid(hostUuid);
        List<ParkingLotSalesSummaryDto> result = new ArrayList<>();
        for (String lotUuid : parkingLotUuids) {
            // 주차장명 조회
            String parkingLotName = "";
            try {
                ParkingLotInfoResponseVo info = parkingLotInternalClient.getParkingLotInfo(lotUuid);
                parkingLotName = info != null ? info.getParkingLotName() : "";
            } catch (Exception e) {
                parkingLotName = "";
            }
            // 월매출 합계
            int monthlySales = dailySettlementRepository.sumMonthlySales(hostUuid, lotUuid, year, month);
            // 주매출 합계 (week가 null이면 0)
            int weeklySales = (week != null) ? dailySettlementRepository.sumWeeklySales(hostUuid, lotUuid, year, week) : 0;
            result.add(new ParkingLotSalesSummaryDto(lotUuid, parkingLotName, monthlySales, weeklySales));
        }
        return result;
    }

    @Override
    public List<ParkingLotWeeklySalesDto> getWeeklySalesAll(String hostUuid, int year, int week) {
        // 1. getParkingLotSalesSummary로 주차장 목록 및 기본정보 조회
        ApiResponse<List<ParkingLotSalesSummaryDto>> response = parkingLotInternalClient.getParkingLotSalesSummary(hostUuid, year, 0, week);
        List<ParkingLotSalesSummaryDto> summaryList = response.getData(); // 실제 메서드명(getData/getResult 등) 확인 필요
        List<ParkingLotWeeklySalesDto> result = new ArrayList<>();
        if (summaryList != null) {
            for (ParkingLotSalesSummaryDto summary : summaryList) {
                String parkingLotUuid = summary.getParkingLotUuid();
                String parkingLotName = summary.getParkingLotName();
                // 2. 각 주차장별 주별 일매출 조회
                List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndYearWeek(
                    parkingLotUuid, year, week
                );
                // 3. 일매출 합산 로직
                List<DailySalesResponseDto> aggregatedSales = aggregateDailySales(dailySales);
                // 4. DTO 생성
                ParkingLotWeeklySalesDto dto = new ParkingLotWeeklySalesDto(
                    parkingLotUuid,
                    parkingLotName,
                    aggregatedSales
                );
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<ParkingLotWeeklySalesDto> getParkingLotsWeeklySalesByRange(String hostUuid, String startDate, String endDate) {
        // 1. 주차장 목록을 DB에서 distinct로 추출
        List<String> parkingLotUuids = dailySettlementRepository.findDistinctParkingLotUuidsByHostUuid(hostUuid);
        List<ParkingLotWeeklySalesDto> result = new ArrayList<>();
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        for (String parkingLotUuid : parkingLotUuids) {
            // 주차장명 조회
            String parkingLotName = "";
            try {
                ParkingLotInfoResponseVo info = parkingLotInternalClient.getParkingLotInfo(parkingLotUuid);
                parkingLotName = info != null ? info.getParkingLotName() : "";
            } catch (Exception e) {
                parkingLotName = "";
            }
            // 2. 각 주차장별로 일매출 조회 (날짜 범위)
            List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndDateRange(
                parkingLotUuid, start, end
            );
            // 3. 일매출 합산 로직
            List<DailySalesResponseDto> aggregatedSales = aggregateDailySales(dailySales);
            ParkingLotWeeklySalesDto dto = new ParkingLotWeeklySalesDto(
                parkingLotUuid,
                parkingLotName,
                aggregatedSales
            );
            result.add(dto);
        }
        return result;
    }
    
    @Override
    public List<DailySalesResponseDto> getWeeklySalesByParkingLot(String hostUuid, String parkingLotUuid, int year, int week) {
        // 주차장별 합산된 일매출 조회 (주별)
        List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndYearWeek(parkingLotUuid, year, week);
        return aggregateDailySales(dailySales);
    }
    
    @Override
    public List<DailySalesResponseDto> getWeeklySalesByParkingLotAndRange(String hostUuid, String parkingLotUuid, String startDate, String endDate) {
        // 주차장별 합산된 일매출 조회 (날짜 범위)
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndDateRange(parkingLotUuid, start, end);
        return aggregateDailySales(dailySales);
    }
    
    @Override
    public List<ParkingLotSalesSummaryDto> getParkingLotSalesSummaryByRange(String hostUuid, String startDate, String endDate) {
        List<String> parkingLotUuids = dailySettlementRepository.findDistinctParkingLotUuidsByHostUuid(hostUuid);
        List<ParkingLotSalesSummaryDto> result = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        for (String parkingLotUuid : parkingLotUuids) {
            // 주차장명 조회
            String parkingLotName = "";
            try {
                ParkingLotInfoResponseVo info = parkingLotInternalClient.getParkingLotInfo(parkingLotUuid);
                parkingLotName = info != null ? info.getParkingLotName() : "";
            } catch (Exception e) {
                parkingLotName = "";
            }
            // 해당 기간의 매출 합계
            List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndDateRange(
                parkingLotUuid, start, end
            );
            int weeklySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).sum();
            // monthlySales는 0 또는 필요시 별도 계산
            result.add(new ParkingLotSalesSummaryDto(parkingLotUuid, parkingLotName, 0, weeklySales));
        }
        return result;
    }
    
    @Override
    public List<ParkingLotSalesSummaryDto> getParkingLotSalesSummaryFlexible(String hostUuid, int year, Integer month, Integer weekOfMonth) {
        // 1. 호스트가 관리하는 모든 주차장 UUID 리스트 조회
        List<String> parkingLotUuids = dailySettlementRepository.findDistinctParkingLotUuidsByHostUuid(hostUuid);
        Map<String, ParkingLotSalesSummaryDto> resultMap = new HashMap<>();

        log.info("Flexible 호출 - year: {}, month: {}, weekOfMonth: {}, 주차장 수: {}", year, month, weekOfMonth, parkingLotUuids.size());

        // 2. 각 주차장별로 주차장명 조회 및 초기화
        for (String parkingLotUuid : parkingLotUuids) {
            String parkingLotName = "";
            try {
                ParkingLotInfoResponseVo info = parkingLotInternalClient.getParkingLotInfo(parkingLotUuid);
                parkingLotName = info != null ? info.getParkingLotName() : "";
            } catch (Exception e) {
                parkingLotName = "";
            }
            resultMap.put(parkingLotUuid, new ParkingLotSalesSummaryDto(parkingLotUuid, parkingLotName, 0, 0));
        }

        // 3. 월매출만 조회 (month만 있으면)
        if (month != null && weekOfMonth == null) {
            log.info("월매출만 조회 시작");
            for (ParkingLotSalesSummaryDto dto : getParkingLotSalesSummary(hostUuid, year, month, null)) {
                resultMap.compute(dto.getParkingLotUuid(), (k, v) -> {
                    if (v != null) {
                        return new ParkingLotSalesSummaryDto(v.getParkingLotUuid(), v.getParkingLotName(), dto.getMonthlySales(), 0);
                    }
                    return dto;
                });
            }
            log.info("월매출만 조회 완료");
        }
        // 4. 주차매출만 조회 (weekOfMonth만 있으면)
        else if (weekOfMonth != null && month == null) {
            log.info("주차매출만 조회 시작 - weekOfMonth: {}", weekOfMonth);
            // 주차매출만 조회하려면 month 정보가 필요하므로 에러 처리
            log.warn("주차매출만 조회하려면 month 정보가 필요합니다");
        }
        // 5. 월매출 + 주차매출 동시 조회 (둘 다 있으면)
        else if (month != null && weekOfMonth != null) {
            log.info("월매출 + 주차매출 동시 조회 시작");
            
            // 월매출 조회
            for (ParkingLotSalesSummaryDto dto : getParkingLotSalesSummary(hostUuid, year, month, null)) {
                resultMap.compute(dto.getParkingLotUuid(), (k, v) -> {
                    if (v != null) {
                        return new ParkingLotSalesSummaryDto(v.getParkingLotUuid(), v.getParkingLotName(), dto.getMonthlySales(), v.getWeeklySales());
                    }
                    return dto;
                });
            }
            
            // 주차매출 조회
            LocalDate[] range = getWeekRange(year, month, weekOfMonth);
            if (range != null) {
                List<ParkingLotSalesSummaryDto> weeklyResults = getParkingLotSalesSummaryByRange(hostUuid, range[0].toString(), range[1].toString());
                for (ParkingLotSalesSummaryDto dto : weeklyResults) {
                    resultMap.compute(dto.getParkingLotUuid(), (k, v) -> {
                        if (v != null) {
                            return new ParkingLotSalesSummaryDto(v.getParkingLotUuid(), v.getParkingLotName(), v.getMonthlySales(), dto.getWeeklySales());
                        }
                        return new ParkingLotSalesSummaryDto(dto.getParkingLotUuid(), dto.getParkingLotName(), 0, dto.getWeeklySales());
                    });
                }
            }
            log.info("월매출 + 주차매출 동시 조회 완료");
        }

        log.info("최종 결과 - {}건", resultMap.size());
        return new ArrayList<>(resultMap.values());
    }

    /**
     * 주간 매출 통계 조회
     */
    @Override
    public List<WeeklySalesStatisticsDto> getWeeklySalesStatistics(String hostUuid, int year, int month, int weekOfMonth) {
        List<String> parkingLotUuids = dailySettlementRepository.findDistinctParkingLotUuidsByHostUuid(hostUuid);
        List<WeeklySalesStatisticsDto> result = new ArrayList<>();
        
        LocalDate[] range = getWeekRange(year, month, weekOfMonth);
        if (range == null) {
            log.warn("주차 범위 계산 실패 - year: {}, month: {}, weekOfMonth: {}", year, month, weekOfMonth);
            return result;
        }
        
        String weekRangeStr = range[0].toString() + " ~ " + range[1].toString();
        
        for (String parkingLotUuid : parkingLotUuids) {
            // 주차장명 조회
            String parkingLotName = "";
            try {
                ParkingLotInfoResponseVo info = parkingLotInternalClient.getParkingLotInfo(parkingLotUuid);
                parkingLotName = info != null ? info.getParkingLotName() : "";
            } catch (Exception e) {
                parkingLotName = "";
            }
            
            // 주간 매출 데이터 조회
            List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndDateRange(
                parkingLotUuid, range[0], range[1]
            );
            
            // 통계 계산
            int totalWeeklySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).sum();
            int totalDays = (int) range[0].until(range[1].plusDays(1), java.time.temporal.ChronoUnit.DAYS);
            int salesDays = dailySales.size();
            double salesRate = totalDays > 0 ? (double) salesDays / totalDays : 0.0;
            
            int maxDailySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).max().orElse(0);
            int minDailySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).min().orElse(0);
            int averageDailySales = totalDays > 0 ? totalWeeklySales / totalDays : 0;
            
            WeeklySalesStatisticsDto statistics = new WeeklySalesStatisticsDto(
                parkingLotUuid, parkingLotName, totalWeeklySales, averageDailySales,
                maxDailySales, minDailySales, totalDays, salesDays, salesRate, weekRangeStr
            );
            
            result.add(statistics);
        }
        
        return result;
    }

    @Override
    public List<WeeklySalesStatisticsDto> getWeeklySalesStatisticsByRange(String hostUuid, String startDate, String endDate) {
        List<String> parkingLotUuids = dailySettlementRepository.findDistinctParkingLotUuidsByHostUuid(hostUuid);
        List<WeeklySalesStatisticsDto> result = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        String weekRangeStr = start.toString() + " ~ " + end.toString();

        for (String parkingLotUuid : parkingLotUuids) {
            // 주차장명 조회
            String parkingLotName = "";
            try {
                ParkingLotInfoResponseVo info = parkingLotInternalClient.getParkingLotInfo(parkingLotUuid);
                parkingLotName = info != null ? info.getParkingLotName() : "";
            } catch (Exception e) {
                parkingLotName = "";
            }
            // 주간 매출 데이터 조회
            List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndDateRange(
                parkingLotUuid, start, end
            );
            // 통계 계산
            int totalWeeklySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).sum();
            int totalDays = (int) start.until(end.plusDays(1), java.time.temporal.ChronoUnit.DAYS);
            int salesDays = dailySales.size();
            double salesRate = totalDays > 0 ? (double) salesDays / totalDays : 0.0;
            int maxDailySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).max().orElse(0);
            int minDailySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).min().orElse(0);
            int averageDailySales = totalDays > 0 ? totalWeeklySales / totalDays : 0;
            WeeklySalesStatisticsDto statistics = new WeeklySalesStatisticsDto(
                parkingLotUuid, parkingLotName, totalWeeklySales, averageDailySales,
                maxDailySales, minDailySales, totalDays, salesDays, salesRate, weekRangeStr
            );
            result.add(statistics);
        }
        return result;
    }

    @Override
    public FlexibleWeeklyStatisticsDto getFlexibleWeeklyStatistics(String hostUuid, String baseDate, Integer daysBefore, Integer daysAfter) {
        // 날짜 범위 계산
        LocalDate base = LocalDate.parse(baseDate);
        int before = daysBefore != null ? daysBefore : 0;
        int after = daysAfter != null ? daysAfter : 0;
        LocalDate start = base.minusDays(before);
        LocalDate end = base.plusDays(after);
        String weekRangeStr = start.toString() + " ~ " + end.toString();

        // 주차장 목록 조회
        List<String> parkingLotUuids = dailySettlementRepository.findDistinctParkingLotUuidsByHostUuid(hostUuid);
        if (parkingLotUuids.isEmpty()) {
            return null;
        }
        // 예시: 첫 번째 주차장만 반환 (여러 주차장 지원 필요시 List로 확장)
        String parkingLotUuid = parkingLotUuids.get(0);
        String parkingLotName = "";
        try {
            ParkingLotInfoResponseVo info = parkingLotInternalClient.getParkingLotInfo(parkingLotUuid);
            parkingLotName = info != null ? info.getParkingLotName() : "";
        } catch (Exception e) {
            parkingLotName = "";
        }
        // 매출 데이터 조회
        List<DailySalesResponseDto> dailySales = dailySettlementRepository.findDailySalesByParkingLotAndDateRange(
            parkingLotUuid, start, end
        );
        int totalWeeklySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).sum();
        int totalDays = (int) start.until(end.plusDays(1), java.time.temporal.ChronoUnit.DAYS);
        // 매출 발생 일수(고유 날짜 개수)
        int salesDays = (int) dailySales.stream()
            .map(DailySalesResponseDto::getDate)
            .distinct()
            .count();
        double salesRate = totalDays > 0 ? (double) salesDays / totalDays : 0.0;
        int maxDailySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).max().orElse(0);
        int minDailySales = dailySales.stream().mapToInt(DailySalesResponseDto::getAmount).min().orElse(0);
        int averageDailySales = totalDays > 0 ? totalWeeklySales / totalDays : 0;
        return new FlexibleWeeklyStatisticsDto(
            parkingLotUuid,
            parkingLotName,
            totalWeeklySales,
            averageDailySales,
            maxDailySales,
            minDailySales,
            totalDays,
            salesDays,
            salesRate,
            weekRangeStr
        );
    }

    /**
     * 월의 n주차(1~5) 날짜 범위 계산 유틸리티
     */
    private LocalDate[] getWeekRange(int year, int month, int weekOfMonth) {
        LocalDate start = LocalDate.of(year, month, 1);
        int startDay = (weekOfMonth - 1) * 7 + 1;
        int endDay = Math.min(weekOfMonth * 7, start.lengthOfMonth());
        if (startDay > start.lengthOfMonth()) {
            return null;
        }
        LocalDate weekStart = LocalDate.of(year, month, startDay);
        LocalDate weekEnd = LocalDate.of(year, month, endDay);
        return new LocalDate[]{weekStart, weekEnd};
    }

    /**
     * 일매출 합산 로직
     */
    private List<DailySalesResponseDto> aggregateDailySales(List<DailySalesResponseDto> dailySales) {
        return dailySales.stream()
                .collect(Collectors.groupingBy(
                    DailySalesResponseDto::getDate
                ))
                .entrySet()
                .stream()
                .filter(e -> e.getKey() != null)
                .map(e -> new DailySalesResponseDto(
                    e.getKey(),
                    e.getValue().stream()
                        .map(dto -> new java.math.BigDecimal(dto.getAmount()))
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                ))
                .sorted(java.util.Comparator.comparing(DailySalesResponseDto::getDate))
                .collect(Collectors.toList());
    }
}