package com.parkmate.batchservice.hostsettlement.application;

import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.DailySalesResponseDto;
import com.parkmate.batchservice.hostsettlement.dto.response.feignforhost.MonthlySalesResponseDto;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface HostSettlementService {

    List<HostParkingLotDto> getAllHostParkingLotPairsFromPayment(LocalDate targetDate);

    List<HostParkingLotDto> getAllHostParkingLotPairsFromPayment(YearMonth yearMonth, SettlementCycle cycle);

    DailySalesResponseDto getDailySales(String hostUuid, String parkingLotUuid, LocalDate date);

    MonthlySalesResponseDto getMonthlySales(String hostUuid, String parkingLotUuid, int year, int month, SettlementCycle cycle);
}
