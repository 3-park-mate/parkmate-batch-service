package com.parkmate.batchservice.hostsettlement.infrastructure.reader;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class DailySalesReader implements ItemReader<DailySettlement> {
    private final Iterator<DailySettlement> iterator;

    public DailySalesReader(DailySettlementRepository repository, String hostUuid, String parkingLotUuid, LocalDate settlementDate) {
        List<DailySettlement> settlements = repository.findByHostUuidAndParkingLotUuidAndSettlementDate(hostUuid, parkingLotUuid, settlementDate);
        this.iterator = settlements.iterator();
        log.info("✅ [DailySalesReader] 일매출 데이터 로드: host={}, lot={}, date={}, count={}", hostUuid, parkingLotUuid, settlementDate, settlements.size());
    }

    @Override
    public DailySettlement read() {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }
}