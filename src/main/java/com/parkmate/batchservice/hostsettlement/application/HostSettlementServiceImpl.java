package com.parkmate.batchservice.hostsettlement.application;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.infrastructure.client.PaymentFeignClient;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.HostSettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HostSettlementServiceImpl implements HostSettlementService {

    private final PaymentFeignClient paymentFeignClient;
    private final HostSettlementRepository hostSettlementRepository;

    @Transactional
    @Override
    public List<HostParkingLotDto> getAllHostParkingLotPairsFromPayment(LocalDate targetDate) {
        return paymentFeignClient.getHostParkingLotPairsByDate(targetDate.toString());
    }

    @Transactional
    @Override
    public List<HostParkingLotDto> getAllHostParkingLotPairsFromPayment(YearMonth yearMonth, SettlementCycle cycle) {
        DateRange range = calculateDateRange(yearMonth, cycle);
        return paymentFeignClient.getHostParkingLotPairsBetween(range.start().toString(), range.end().toString());
    }

    @Transactional
    @Override
    public DailySalesResponseDto getDailySales(String hostUuid, String parkingLotUuid, LocalDate date) {
        List<HostSettlement> settlements = hostSettlementRepository
                .findByHostUuidAndParkingLotUuidAndSettlementDate(hostUuid, parkingLotUuid, date);

        BigDecimal totalSales = sumTotalSales(settlements);

        return DailySalesResponseDto.builder()
                .date(date)
                .totalSalesAmount(totalSales)
                .build();
    }

    @Transactional
    @Override
    public MonthlySalesResponseDto getMonthlySales(String hostUuid, String parkingLotUuid, int year, int month, SettlementCycle cycle) {
        YearMonth yearMonth = YearMonth.of(year, month);
        DateRange range = calculateDateRange(yearMonth, cycle);

        List<HostSettlement> settlements = hostSettlementRepository
                .findByHostUuidAndParkingLotUuidAndSettlementDateBetweenAndSettlementCycle(
                        hostUuid,
                        parkingLotUuid,
                        range.start(),
                        range.end(),
                        cycle
                );

        BigDecimal totalSales = sumTotalSales(settlements);

        return MonthlySalesResponseDto.builder()
                .yearMonth(yearMonth.toString())
                .totalSalesAmount(totalSales)
                .build();
    }

    private BigDecimal sumTotalSales(List<HostSettlement> settlements) {
        if (settlements == null || settlements.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return settlements.stream()
                .map(HostSettlement::getTotalSalesAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DateRange calculateDateRange(YearMonth yearMonth, SettlementCycle cycle) {
        LocalDate start = (cycle == SettlementCycle.FIFTEEN) ? yearMonth.atDay(1) : yearMonth.atDay(16);
        LocalDate end = (cycle == SettlementCycle.FIFTEEN) ? yearMonth.atDay(15) : yearMonth.atEndOfMonth();
        return new DateRange(start, end);
    }

    private record DateRange(LocalDate start, LocalDate end) {}
}