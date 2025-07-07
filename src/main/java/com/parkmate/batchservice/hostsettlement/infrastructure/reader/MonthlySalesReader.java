package com.parkmate.batchservice.hostsettlement.infrastructure.reader;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MonthlySalesReader implements ItemReader<List<DailySettlement>> {
    private final Iterator<List<DailySettlement>> iterator;

    public MonthlySalesReader(DailySettlementRepository repository, String hostUuid, String parkingLotUuid, LocalDate startDate, LocalDate endDate, SettlementCycle cycle) {
        log.info("[월매출 집계 파라미터] hostUuid={}, parkingLotUuid={}, 기간=[{}~{}], cycle={}", hostUuid, parkingLotUuid, startDate, endDate, cycle);
        List<DailySettlement> settlements = repository.findByHostUuidAndParkingLotUuidAndSettlementDateBetweenAndSettlementCycle(
                hostUuid, parkingLotUuid, startDate, endDate, SettlementCycle.DAILY
        );
        Map<String, List<DailySettlement>> grouped = settlements.stream()
                .collect(Collectors.groupingBy(s -> s.getHostUuid() + "-" + s.getParkingLotUuid()));
        this.iterator = grouped.values().iterator();
        log.info("✅ [MonthlySalesReader] 월매출 집계 기간 [{} ~ {}] 그룹 수: {}", startDate, endDate, grouped.size());
    }

    @Override
    public List<DailySettlement> read() {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }
}